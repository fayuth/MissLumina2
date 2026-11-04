package com.misslumina.config;

import java.util.List;

/**
 * Constantes generales de la aplicacion.
 *
 * <p>Se declaran con {@code public static final} para que sean valores fijos
 * y compartidos. Los valores propios del dominio (IVA, estados, roles)
 * tambien aparecen en las clases del modelo donde se utilizan.</p>
 */
public final class Constantes {

    /** IVA colombiano vigente: 19%. */
    public static final double IVA = 0.19;

    /** Stock a partir del cual se considera que un producto esta bajo. */
    public static final int STOCK_MINIMO = 5;

    public static final String ROL_CLIENTE = "CLIENTE";
    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_CONFIRMADO = "CONFIRMADO";
    public static final String ESTADO_ENVIADO = "ENVIADO";
    public static final String ESTADO_ENTREGADO = "ENTREGADO";
    public static final String ESTADO_CANCELADO = "CANCELADO";

    public static final List<String> METODOS_PAGO = List.of(
            "Tarjeta de credito",
            "Tarjeta debito",
            "PSE",
            "Nequi",
            "Efectivo contraentrega"
    );

    public static final List<String> ESTADOS_PEDIDO = List.of(
            ESTADO_PENDIENTE,
            ESTADO_CONFIRMADO,
            ESTADO_ENVIADO,
            ESTADO_ENTREGADO,
            ESTADO_CANCELADO
    );

    private Constantes() {
        // No se instancia.
    }
}
