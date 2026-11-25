package com.ipartek.modelo;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilaMedicion {
	private String municipio;
	private String estacion;
	// contaminante -> "12,3 µg/m3" (si no hay valor, no está en el mapa)
	private Map<String, String> valores;
}