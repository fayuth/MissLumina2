package com.misslumina.exception;

public class PedidoDuplicadoException extends MissLuminaException {

    public PedidoDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public PedidoDuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
