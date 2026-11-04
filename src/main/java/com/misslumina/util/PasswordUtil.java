package com.misslumina.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordUtil {

    public static final String ALGORITMO = "SHA-256";

    private PasswordUtil() {
    }

    public static String hashear(String contrasena) {
        if (contrasena == null) {
            throw new IllegalArgumentException("La contrasena no puede ser nula");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            byte[] hash = digest.digest(contrasena.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("El algoritmo SHA-256 no esta disponible", e);
        }
    }

    public static boolean verificar(String contrasenaPlano, String hashGuardado) {
        if (contrasenaPlano == null || hashGuardado == null) {
            return false;
        }
        return hashear(contrasenaPlano).equalsIgnoreCase(hashGuardado);
    }
}
