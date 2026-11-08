package com.misslumina.controller.dto;

import com.misslumina.model.ecommerce.DetallePedido;
import com.misslumina.model.ecommerce.Pedido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoDetalleDTO(
        Long id,
        UUID idTransaccion,
        LocalDate fechaPedido,
        LocalDateTime fechaHoraPedido,
        String estado,
        double total,
        String metodoPago,
        String cliente,
        List<LineaDTO> lineas
) {
    public record LineaDTO(Long productoId, String producto, int cantidad,
                           double precioUnitario, double subtotal) {
        public static LineaDTO desde(DetallePedido detalle) {
            return new LineaDTO(
                    detalle.getProducto() == null ? null : detalle.getProducto().getId(),
                    detalle.getProductoNombre(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotal());
        }
    }

    public static PedidoDetalleDTO desde(Pedido pedido) {
        return new PedidoDetalleDTO(
                pedido.getId(),
                pedido.getIdTransaccion(),
                pedido.getFechaPedido(),
                pedido.getFechaHoraPedido(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getMetodoPago(),
                pedido.getClienteNombre(),
                pedido.getDetalles().stream().map(LineaDTO::desde).toList());
    }
}
