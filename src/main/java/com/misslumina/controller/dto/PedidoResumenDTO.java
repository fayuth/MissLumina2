package com.misslumina.controller.dto;

import com.misslumina.model.Pedido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** DTO para listar pedidos sin exponer entidades JPA ni relaciones lazy. */
public record PedidoResumenDTO(
        Long id,
        UUID idTransaccion,
        LocalDate fechaPedido,
        LocalDateTime fechaHoraPedido,
        String estado,
        double total,
        String metodoPago,
        String cliente,
        int unidades
) {
    public static PedidoResumenDTO desde(Pedido pedido) {
        return new PedidoResumenDTO(
                pedido.getId(),
                pedido.getIdTransaccion(),
                pedido.getFechaPedido(),
                pedido.getFechaHoraPedido(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getMetodoPago(),
                pedido.getClienteNombre(),
                pedido.calcularUnidadesVendidas());
    }
}
