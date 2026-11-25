package com.ipartek.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ipartek.auxiliar.ClienteHttp;
import com.ipartek.auxiliar.Json;
import com.ipartek.modelo.Estacion;

@Service
public class EstacionServicioImp implements EstacionServicio {

	@Value("${url.euskadi}")
	private String url;
	@Value("${aire.condado.bizkaia}")
	private String condado;

	private final RestClient restClient = ClienteHttp.crear();

	@Override
	public List<Estacion> obtenerBizkaia() {
		try {
			Map<String, Object> geo = Json.mapa(restClient.get().uri(url + "/stations").retrieve().body(Object.class));
			List<Estacion> lista = new ArrayList<>();

			for (Object f : Json.lista(geo.get("features"))) {
				Map<String, Object> feature = Json.mapa(f);
				Map<String, Object> props = Json.mapa(feature.get("properties"));
				Map<String, Object> loc = Json.mapa(props.get("location"));

				
				if (!condado.equals(Json.texto(loc, "countyId"))) {
					continue;
				}

				String id = Json.texto(props, "id");
				String nombre = Json.texto(props, "name");
				if (nombre == null) {
					nombre = "";
				}
				String municipio = Json.texto(loc, "municipality");

				
				List<Object> coords = Json.lista(Json.mapa(feature.get("geometry")).get("coordinates"));
				Double lon = coords.size() >= 2 ? Json.numero(coords.get(0)) : null;
				Double lat = coords.size() >= 2 ? Json.numero(coords.get(1)) : null;

				lista.add(new Estacion(id, nombre, municipio != null ? municipio : nombre, lat, lon));
			}
			return lista;
		} catch (Exception e) {
			e.printStackTrace();  
			return null;
		}
	}
}


//https://api.euskadi.eus/air-quality/stations