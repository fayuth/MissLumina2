package com.misslumina.controller;

import com.misslumina.service.catalog.ImagenService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.Duration;

@RestController
public class ImagenController {

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    @GetMapping("/uploads/productos/{archivo:.+}")
    public ResponseEntity<Resource> ver(@PathVariable String archivo) {
        Path ruta = imagenService.resolverArchivo(archivo);
        if (ruta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagenService.tipoContenido(ruta)))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(new FileSystemResource(ruta));
    }
}
