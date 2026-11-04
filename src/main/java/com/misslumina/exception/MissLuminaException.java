package com.misslumina.exception;

public class MissLuminaException extends RuntimeException {

    public MissLuminaException(String mensaje) {
        super(mensaje);
    }

    public MissLuminaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
