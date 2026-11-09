package com.misslumina.config;

import java.util.List;

public final class Constantes {

    public static final double IVA = 0.19;

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
    }
}
