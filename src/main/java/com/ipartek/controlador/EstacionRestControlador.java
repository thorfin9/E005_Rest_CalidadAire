package com.ipartek.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipartek.modelo.Estacion;
import com.ipartek.servicios.EstacionServicio;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("${api.estaciones}")
public class EstacionRestControlador {

	@Autowired
	private EstacionServicio estacionServ;

	@Operation(summary = "Estaciones de calidad del aire de Bizkaia")
	@GetMapping("")
	public ResponseEntity<?> obtenerTodas() {
		try {
			List<Estacion> lista = estacionServ.obtenerBizkaia();
			if (lista == null) {
				return ResponseEntity.internalServerError().build();
			}
			if (lista.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(lista);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}