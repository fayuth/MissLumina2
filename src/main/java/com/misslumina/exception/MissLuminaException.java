package com.misslumina.exception;

/**
 * Excepcion base de todas las reglas de negocio de Miss Lumina.
 * Se utiliza para enviar mensajes claros al usuario sin exponer detalles
 * tecnicos de la base de datos.
 */
public class MissLuminaException extends RuntimeException {

    public MissLuminaException(String mensaje) {
        super(mensaje);
    }

    public MissLuminaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
