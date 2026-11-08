package com.misslumina.service.admin;

import com.misslumina.model.ecommerce.Pedido;
import com.misslumina.model.catalog.Producto;
import com.misslumina.repository.DetallePedidoRepository;
import com.misslumina.repository.PedidoRepository;
import com.misslumina.repository.ProductoRepository;
import com.misslumina.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public ReporteService(PedidoRepository pedidoRepository,
                          DetallePedidoRepository detallePedidoRepository,
                          UsuarioRepository usuarioRepository,
                          ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    public record Resumen(double ventas, long pedidos, long unidades, long clientes,
                          long productos, double ticketPromedio, long pendientes,
                          long stockBajo) {
    }

    public record FilaReporte(String etiqueta, long unidades, double valor) {
    }

    public record EstadoReporte(String estado, long pedidos, double valor) {
    }

    @Transactional(readOnly = true)
    public Resumen resumen() {
        double ventas = valor(pedidoRepository.totalVentas());
        long pedidos = pedidoRepository.contarVentas();
        long unidades = valorLargo(detallePedidoRepository.unidadesVendidas());
        long clientes = usuarioRepository.countByRol("CLIENTE");
        long productos = productoRepository.count();
        double ticket = pedidos == 0 ? 0.0 : ventas / pedidos;
        long pendientes = pedidoRepository.countByEstado(Pedido.ESTADO_PENDIENTE);
        long stockBajo = productoRepository.contarStockBajo(Producto.STOCK_MINIMO);
        return new Resumen(ventas, pedidos, unidades, clientes, productos, ticket,
                pendientes, stockBajo);
    }

    @Transactional(readOnly = true)
    public Resumen resumen(LocalDate desde, LocalDate hasta) {
        double ventas = valor(pedidoRepository.totalVentasEntre(desde, hasta));
        long pedidos = pedidoRepository.contarVentasEntre(desde, hasta);
        long unidades = valorLargo(detallePedidoRepository.unidadesVendidasEntre(desde, hasta));
        long clientes = usuarioRepository.countByRol("CLIENTE");
        long productos = productoRepository.count();
        double ticket = pedidos == 0 ? 0.0 : ventas / pedidos;
        long pendientes = pedidoRepository.countByEstado(Pedido.ESTADO_PENDIENTE);
        long stockBajo = productoRepository.contarStockBajo(Producto.STOCK_MINIMO);
        return new Resumen(ventas, pedidos, unidades, clientes, productos, ticket,
                pendientes, stockBajo);
    }

    @Transactional(readOnly = true)
    public List<FilaReporte> productosMasVendidos() {
        return convertirFilas(detallePedidoRepository.productosMasVendidos());
    }

    @Transactional(readOnly = true)
    public List<FilaReporte> productosMasVendidos(LocalDate desde, LocalDate hasta) {
        return convertirFilas(detallePedidoRepository.productosMasVendidosEntre(desde, hasta));
    }

    @Transactional(readOnly = true)
    public List<FilaReporte> ventasPorCategoria() {
        return convertirFilas(detallePedidoRepository.ventasPorCategoria());
    }

    @Transactional(readOnly = true)
    public List<FilaReporte> ventasPorCategoria(LocalDate desde, LocalDate hasta) {
        return convertirFilas(detallePedidoRepository.ventasPorCategoriaEntre(desde, hasta));
    }

    @Transactional(readOnly = true)
    public List<EstadoReporte> ventasPorEstado() {
        return convertirEstados(pedidoRepository.resumenPorEstado());
    }

    @Transactional(readOnly = true)
    public List<EstadoReporte> ventasPorEstado(LocalDate desde, LocalDate hasta) {
        return convertirEstados(pedidoRepository.resumenPorEstadoEntre(desde, hasta));
    }

    @Transactional(readOnly = true)
    public List<FilaReporte> ventasPorDia(LocalDate desde, LocalDate hasta) {
        Map<LocalDate, double[]> acumulado = new LinkedHashMap<>();
        for (Pedido pedido : pedidoRepository.buscarPorRangoConDetalles(desde, hasta)) {
            if (pedido.estaCancelado()) {
                continue;
            }
            double[] valores = acumulado.computeIfAbsent(pedido.getFechaPedido(),
                    clave -> new double[2]);
            valores[0] += pedido.calcularUnidadesVendidas();
            valores[1] += pedido.getTotal();
        }
        List<FilaReporte> filas = new ArrayList<>();
        for (Map.Entry<LocalDate, double[]> entry : acumulado.entrySet()) {
            filas.add(new FilaReporte(entry.getKey().toString(),
                    (long) entry.getValue()[0], entry.getValue()[1]));
        }
        return filas;
    }

    private List<FilaReporte> convertirFilas(List<Object[]> datos) {
        List<FilaReporte> filas = new ArrayList<>();
        for (Object[] fila : datos) {
            filas.add(new FilaReporte(
                    String.valueOf(fila[0]),
                    valorLargo(fila[1]),
                    valor(fila[2])));
        }
        return filas;
    }

    private List<EstadoReporte> convertirEstados(List<Object[]> datos) {
        List<EstadoReporte> filas = new ArrayList<>();
        for (Object[] fila : datos) {
            filas.add(new EstadoReporte(
                    String.valueOf(fila[0]),
                    valorLargo(fila[1]),
                    valor(fila[2])));
        }
        return filas;
    }

    private double valor(Object numero) {
        if (numero == null) {
            return 0.0;
        }
        if (numero instanceof Number n) {
            return n.doubleValue();
        }
        return Double.parseDouble(numero.toString());
    }

    private long valorLargo(Object numero) {
        if (numero == null) {
            return 0L;
        }
        if (numero instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(numero.toString());
    }
}
