package com.misslumina.service.catalog;

import com.misslumina.exception.DatosInvalidosException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ImagenService {

    private static final Logger log = LoggerFactory.getLogger(ImagenService.class);

    public static final int TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;

    private static final Map<String, String> EXTENSION_POR_TIPO = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp",
            "image/gif", "gif");

    private static final Map<String, String> TIPO_POR_EXTENSION = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "webp", "image/webp",
            "gif", "image/gif");

    private final Path directorioProductos;

    public ImagenService(@Value("${app.uploads.dir:uploads}") String directorioBase) {
        this.directorioProductos = Paths.get(directorioBase)
                .toAbsolutePath()
                .normalize()
                .resolve("productos");
    }

    @PostConstruct
    public void crearDirectorio() {
        try {
            Files.createDirectories(directorioProductos);
            log.info("Directorio de imagenes disponible en {}", directorioProductos);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear el directorio de imagenes: " + directorioProductos, e);
        }
    }

    public String guardar(String nombreOriginal, String contentType, byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new DatosInvalidosException("La imagen esta vacia.");
        }
        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new DatosInvalidosException("La imagen supera el limite de 5 MB.");
        }

        String extension = resolverExtension(nombreOriginal, contentType);
        if (extension == null || !coincideConFirma(extension, contenido)) {
            throw new DatosInvalidosException(
                    "Formato no permitido. Use una imagen PNG, JPG, WEBP o GIF.");
        }

        String nombreArchivo = UUID.randomUUID().toString().replace("-", "")
                + "." + extension;
        Path destino = directorioProductos.resolve(nombreArchivo).normalize();
        if (!destino.startsWith(directorioProductos)) {
            throw new DatosInvalidosException("El nombre de la imagen no es valido.");
        }

        try {
            Files.write(destino, contenido);
        } catch (IOException e) {
            log.error("No se pudo guardar la imagen {}", nombreArchivo, e);
            throw new DatosInvalidosException("No se pudo guardar la imagen.");
        }

        log.info("Imagen guardada: {} ({} bytes)", destino, contenido.length);
        return "/uploads/productos/" + nombreArchivo;
    }

    public Path resolverArchivo(String nombreArchivo) {
        if (nombreArchivo == null
                || !nombreArchivo.matches("[a-f0-9]{32}\\.(png|jpg|jpeg|webp|gif)")) {
            return null;
        }
        Path archivo = directorioProductos.resolve(nombreArchivo).normalize();
        if (!archivo.startsWith(directorioProductos) || !Files.isRegularFile(archivo)) {
            return null;
        }
        return archivo;
    }

    public String tipoContenido(Path archivo) {
        String nombre = archivo.getFileName().toString().toLowerCase(Locale.ROOT);
        int punto = nombre.lastIndexOf('.');
        String extension = punto >= 0 ? nombre.substring(punto + 1) : "";
        return TIPO_POR_EXTENSION.getOrDefault(extension, "application/octet-stream");
    }

    public void eliminarSiEsSubida(String url) {
        if (url == null || !url.startsWith("/uploads/productos/")) {
            return;
        }
        String nombreArchivo = url.substring("/uploads/productos/".length());
        Path archivo = resolverArchivo(nombreArchivo);
        if (archivo == null) {
            return;
        }
        try {
            if (Files.deleteIfExists(archivo)) {
                log.info("Imagen eliminada: {}", archivo);
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar la imagen {}", archivo, e);
        }
    }

    private String resolverExtension(String nombreOriginal, String contentType) {
        String tipo = contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        int separador = tipo.indexOf(';');
        if (separador >= 0) {
            tipo = tipo.substring(0, separador).trim();
        }

        String extension = EXTENSION_POR_TIPO.get(tipo);
        if (extension != null) {
            return extension;
        }

        if (nombreOriginal == null) {
            return null;
        }
        String nombre = nombreOriginal.toLowerCase(Locale.ROOT);
        int punto = nombre.lastIndexOf('.');
        if (punto < 0 || punto == nombre.length() - 1) {
            return null;
        }
        String extensionArchivo = nombre.substring(punto + 1);
        if ("jpeg".equals(extensionArchivo)) {
            return "jpg";
        }
        return TIPO_POR_EXTENSION.containsKey(extensionArchivo)
                ? extensionArchivo : null;
    }

    private boolean coincideConFirma(String extension, byte[] datos) {
        return switch (extension) {
            case "png" -> datos.length >= 8
                    && datos[0] == (byte) 0x89
                    && datos[1] == 0x50
                    && datos[2] == 0x4E
                    && datos[3] == 0x47
                    && datos[4] == 0x0D
                    && datos[5] == 0x0A
                    && datos[6] == 0x1A
                    && datos[7] == 0x0A;
            case "jpg" -> datos.length >= 3
                    && datos[0] == (byte) 0xFF
                    && datos[1] == (byte) 0xD8
                    && datos[2] == (byte) 0xFF;
            case "gif" -> datos.length >= 6
                    && datos[0] == 'G'
                    && datos[1] == 'I'
                    && datos[2] == 'F'
                    && datos[3] == '8'
                    && (datos[4] == '7' || datos[4] == '9')
                    && datos[5] == 'a';
            case "webp" -> datos.length >= 12
                    && datos[0] == 'R'
                    && datos[1] == 'I'
                    && datos[2] == 'F'
                    && datos[3] == 'F'
                    && datos[8] == 'W'
                    && datos[9] == 'E'
                    && datos[10] == 'B'
                    && datos[11] == 'P';
            default -> false;
        };
    }
}
