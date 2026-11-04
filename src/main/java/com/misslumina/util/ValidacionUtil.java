package com.misslumina.util;

import java.util.regex.Pattern;

public final class ValidacionUtil {

    private static final Pattern CORREO =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO =
            Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private ValidacionUtil() {
    }

    public static boolean textoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    public static boolean correoValido(String correo) {
        return correo != null && CORREO.matcher(correo.trim()).matches();
    }

    public static boolean telefonoValido(String telefono) {
        return telefono == null || telefono.trim().isEmpty()
                || TELEFONO.matcher(telefono.trim()).matches();
    }

    public static String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
