package com.misslumina.controller;

import com.misslumina.model.catalog.Producto;
import com.misslumina.service.catalog.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String tono) {
        return productoService.buscar(texto, categoria, marca, tono);
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @GetMapping("/destacados")
    public List<Producto> destacados() {
        return productoService.productosDestacados();
    }
}
