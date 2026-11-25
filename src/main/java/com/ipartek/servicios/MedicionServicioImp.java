package com.ipartek.servicios;

import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ipartek.auxiliar.ClienteHttp;
import com.ipartek.auxiliar.Json;
import com.ipartek.modelo.Estacion;
import com.ipartek.modelo.FilaMedicion;
import com.ipartek.modelo.TablaMediciones;

@Service
public class MedicionServicioImp implements MedicionServicio {

	@Value("${url.euskadi}")
	private String url;
	@Value("${aire.condado.bizkaia}")
	private String condado;
	@Value("${aire.horas.atras}")
	private int horas;

	@Autowired
	private EstacionServicio estacionServ;

	private final RestClient restClient = ClienteHttp.crear();

	private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
	
	private static final Set<String> METEO = Set.of("d.vien", "h", "p", "r", "r.uva", "tº", "v.vien",
			"precipitación", "precipitacion");
	
	private static final List<String> ORDEN = List.of("SO2", "NO", "NO2", "NOX", "O3", "CO", "PM10", "PM2,5");

	private record Lectura(String fecha, double valor, String unidad) {
	}

	@Override
	public TablaMediciones obtenerUltimas() {
		try {
			LocalDateTime hasta = LocalDateTime.now(ZoneOffset.UTC);
			LocalDateTime desde = hasta.minusHours(horas);

			Object raiz = restClient.get()
					.uri(url + "/measurements/hourly/counties/{c}/from/{d}/to/{h}", condado, FECHA.format(desde),
							FECHA.format(hasta))
					.retrieve().body(Object.class);

			Map<String, Map<String, Lectura>> ultimas = leerUltimas(raiz);

			Set<String> contaminantes = new TreeSet<>(comparadorContaminantes());
			List<FilaMedicion> filas = new ArrayList<>();
			DecimalFormat df = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-ES")));

			for (Estacion est : estacionServ.obtenerBizkaia()) {
				Map<String, Lectura> lecturas = ultimas.get(est.getId());
				if (lecturas == null) {
					lecturas = ultimas.get(est.getNombre().toLowerCase());
				}
				Map<String, String> valores = new HashMap<>();
				if (lecturas != null) {
					for (Map.Entry<String, Lectura> en : lecturas.entrySet()) {
						Lectura l = en.getValue();
						valores.put(en.getKey(), df.format(l.valor()) + (l.unidad() != null ? " " + l.unidad() : ""));
						contaminantes.add(en.getKey());
					}
				}
				filas.add(new FilaMedicion(est.getMunicipio(), est.getNombre(), valores));
			}

			// orden alfabetico por municipio (y luego por estacion)
			Collator c = Collator.getInstance(Locale.forLanguageTag("es-ES"));
			filas.sort(Comparator.comparing(FilaMedicion::getMunicipio, c).thenComparing(FilaMedicion::getEstacion, c));

			return new TablaMediciones(new ArrayList<>(contaminantes), filas);
		} catch (Exception e) {
			return null;
		}
	}

	
	private Map<String, Map<String, Lectura>> leerUltimas(Object raiz) {
		Map<String, Map<String, Lectura>> ultimas = new HashMap<>();

		for (Object r : registros(raiz)) {
			Map<String, Object> reg = Json.mapa(r);
			String fecha = Json.texto(reg, "date");
			if (fecha == null) {
				continue;
			}
			for (Object e : Json.lista(reg.get("station"))) {
				Map<String, Object> est = Json.mapa(e);

				
				if (est.get("airQualityStation") == null) {
					continue;
				}
				String id = Json.texto(est, "id");
				String nombre = Json.texto(est, "name");
				String clave = id != null ? id : (nombre != null ? nombre.toLowerCase() : null);
				if (clave == null) {
					continue;
				}

				for (Object l : Json.lista(est.get("measurements"))) {
					Map<String, Object> lec = Json.mapa(l);
					String cont = Json.texto(lec, "name");
					Double valor = Json.numero(lec.get("value"));
					if (cont == null || valor == null || valor == 0 || METEO.contains(cont.toLowerCase())
							|| sinDatos(lec)) {
						continue;
					}
					Map<String, Lectura> deEstacion = ultimas.computeIfAbsent(clave, k -> new HashMap<>());
					Lectura previa = deEstacion.get(cont);
					if (previa == null || fecha.compareTo(previa.fecha()) > 0) {
						deEstacion.put(cont, new Lectura(fecha, valor, Json.texto(lec, "unit")));
					}
				}
			}
		}
		return ultimas;
	}

	private List<Object> registros(Object raiz) {
		if (raiz instanceof List<?> l) {
			return new ArrayList<Object>(l);
		}
		for (Object v : Json.mapa(raiz).values()) {
			if (v instanceof List<?> l) {
				return new ArrayList<Object>(l);
			}
		}
		return new ArrayList<>();
	}

	
	private List<Object> lecturasDe(Map<String, Object> nodo) {
		for (String clave : List.of("pollutants", "measurements", "parameters", "values", "data")) {
			if (nodo.get(clave) instanceof List<?> l) {
				return new ArrayList<Object>(l);
			}
		}
		for (Object v : nodo.values()) {
			if (v instanceof List<?> l && !l.isEmpty() && l.get(0) instanceof Map<?, ?> m && m.containsKey("value")) {
				return new ArrayList<Object>(l);
			}
		}
		return new ArrayList<>();
	}

	private boolean sinDatos(Map<String, Object> lec) {
		for (Object v : lec.values()) {
			if (v instanceof String s && s.toLowerCase().contains("sin datos")) {
				return true;
			}
		}
		return false;
	}

	private Comparator<String> comparadorContaminantes() {
		return Comparator.comparingInt((String s) -> {
			int i = ORDEN.indexOf(s.toUpperCase());
			return i < 0 ? ORDEN.size() : i;
		}).thenComparing(String.CASE_INSENSITIVE_ORDER);
	}
}

//https://api.euskadi.eus/air-quality/measurements/hourly/counties/48/from/2026-11-24T06:00/to/2026-11-24T09:00