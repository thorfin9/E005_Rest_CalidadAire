package com.ipartek.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estacion {
	private String id;
	private String nombre;
	private String municipio;
	private Double latitud;
	private Double longitud;
}