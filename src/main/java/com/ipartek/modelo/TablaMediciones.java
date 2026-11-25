package com.ipartek.modelo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TablaMediciones {
	private List<String> contaminantes; // columnas
	private List<FilaMedicion> filas;
}