package com.ipartek.auxiliar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Json {

	@SuppressWarnings("unchecked")
	public static Map<String, Object> mapa(Object o) {
		return o instanceof Map ? (Map<String, Object>) o : new HashMap<>();
	}

	public static List<Object> lista(Object o) {
		if (o instanceof List<?> l) {
			return new ArrayList<Object>(l);
		}
		if (o instanceof Map) {
			return List.of(o);
		}
		return new ArrayList<>();
	}

	// devuelve el primer valor que exista entre las claves indicadas
	public static String texto(Map<String, Object> m, String... claves) {
		for (String c : claves) {
			Object v = m.get(c);
			if (v instanceof Map<?, ?> sub) {
				v = sub.get("name") != null ? sub.get("name") : sub.get("nombre");
			}
			if (v instanceof Number n && n.doubleValue() == Math.floor(n.doubleValue())) {
				return String.valueOf(n.longValue());
			}
			if (v != null && !v.toString().isBlank()) {
				return v.toString().trim();
			}
		}
		return null;
	}

	public static Double numero(Object v) {
		if (v instanceof Number n) {
			return n.doubleValue();
		}
		if (v == null) {
			return null;
		}
		try {
			return Double.parseDouble(v.toString().trim().replace(',', '.'));
		} catch (NumberFormatException e) {
			return null;
		}
	}
}