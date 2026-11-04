package com.misslumina.exception;

/**
 * Excepcion para errores ocurridos durante la confirmacion de una transaccion
 * de pedido. Permite distinguir un fallo de base de datos de una validacion de
 * negocio y provoca el rollback de Spring.
 */
public class TransaccionException extends MissLuminaException {

    public TransaccionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
