package com.ipartek.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipartek.modelo.TablaMediciones;
import com.ipartek.servicios.MedicionServicio;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("${api.mediciones}")
public class MedicionRestControlador {

	@Autowired
	private MedicionServicio medicionServ;

	@Operation(summary = "Ultimas mediciones de las estaciones de Bizkaia")
	@GetMapping("")
	public ResponseEntity<?> obtenerUltimas() {
		try {
			TablaMediciones tabla = medicionServ.obtenerUltimas();
			if (tabla == null) {
				return ResponseEntity.internalServerError().build();
			}
			return ResponseEntity.ok(tabla);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}