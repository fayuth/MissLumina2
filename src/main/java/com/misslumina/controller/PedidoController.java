package com.misslumina.controller;

import com.misslumina.controller.dto.PedidoDetalleDTO;
import com.misslumina.controller.dto.PedidoResumenDTO;
import com.misslumina.model.ecommerce.Pedido;
import com.misslumina.service.ecommerce.PedidoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoResumenDTO> listar() {
        return pedidoService.listarTodos().stream()
                .map(PedidoResumenDTO::desde)
                .toList();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<PedidoResumenDTO> listarPorUsuario(@PathVariable Long usuarioId) {
        List<Pedido> pedidos = pedidoService.listarPorUsuario(usuarioId);
        return pedidos.stream().map(PedidoResumenDTO::desde).toList();
    }

    @GetMapping("/{id}")
    public PedidoDetalleDTO obtener(@PathVariable Long id) {
        return PedidoDetalleDTO.desde(pedidoService.buscarPorId(id));
    }
}
