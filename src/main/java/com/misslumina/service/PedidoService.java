package com.misslumina.service;

import com.misslumina.config.Constantes;
import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.exception.PedidoDuplicadoException;
import com.misslumina.exception.ProductoNoEncontradoException;
import com.misslumina.exception.TransaccionException;
import com.misslumina.model.Carrito;
import com.misslumina.model.DetallePedido;
import com.misslumina.model.ItemCarrito;
import com.misslumina.model.Pedido;
import com.misslumina.model.Producto;
import com.misslumina.model.Usuario;
import com.misslumina.repository.PedidoRepository;
import com.misslumina.repository.ProductoRepository;
import com.misslumina.repository.UsuarioRepository;
import com.misslumina.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Pedido registrarPedido(Usuario usuario, Carrito carrito, String metodoPago) {
        if (usuario == null || usuario.getId() == null) {
            throw new DatosInvalidosException("Debe iniciar sesion para registrar el pedido.");
        }
        if (carrito == null || carrito.estaVacio()) {
            throw new DatosInvalidosException("El carrito esta vacio. Agregue productos antes de comprar.");
        }
        if (ValidacionUtil.textoVacio(metodoPago)) {
            throw new DatosInvalidosException("Debe seleccionar un metodo de pago.");
        }

        Usuario usuarioGestionado = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new DatosInvalidosException("El usuario no existe."));

        Pedido pedido = new Pedido(usuarioGestionado, metodoPago.trim());
        pedido.generarTransaccion();

        if (pedidoRepository.existsByIdTransaccion(pedido.getIdTransaccion())) {
            throw new PedidoDuplicadoException(
                    "Ya existe una transaccion con el identificador generado. Intente nuevamente.");
        }

        for (ItemCarrito item : carrito.getItems()) {
            if (item.getProducto() == null || item.getProducto().getId() == null) {
                throw new ProductoNoEncontradoException("Uno de los productos del carrito no existe.");
            }

            Producto producto = productoRepository
                    .buscarPorIdParaActualizar(item.getProducto().getId())
                    .orElseThrow(() -> new ProductoNoEncontradoException(
                            "El producto " + item.getProducto().getNombre() + " ya no existe."));

            if (!producto.hayStock(item.getCantidad())) {
                throw new com.misslumina.exception.StockInsuficienteException(
                        "Stock insuficiente de " + producto.getNombre()
                                + ". Disponible: " + producto.getStock()
                                + ", solicitado: " + item.getCantidad());
            }
            pedido.agregarDetalle(producto, item.getCantidad());
        }

        pedido.registrarPedido();

        try {
            Pedido guardado = pedidoRepository.save(pedido);
            pedidoRepository.flush();

            for (DetallePedido detalle : guardado.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.descontarStock(detalle.getCantidad());
                productoRepository.save(producto);
            }

            log.info("Pedido registrado: id={}, uuid={}, total={}, unidades={}",
                    guardado.getId(), guardado.getIdTransaccion(), guardado.getTotal(),
                    guardado.calcularUnidadesVendidas());
            return guardado;
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al registrar pedido", e);
            throw new PedidoDuplicadoException(
                    "No se pudo registrar el pedido porque la transaccion ya existe o los datos no son validos.",
                    e);
        } catch (DataAccessException e) {
            log.error("Error de base de datos al confirmar la transaccion del pedido", e);
            throw new TransaccionException(
                    "No se pudo completar la transaccion del pedido. Intente nuevamente.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorUsuario(Long usuarioId) {
        return pedidoRepository.buscarPorUsuarioConDetalles(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.buscarTodosConDetalles();
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorRango(java.time.LocalDate desde, java.time.LocalDate hasta) {
        return pedidoRepository.buscarPorRangoConDetalles(desde, hasta);
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.buscarPorIdConDetalles(id)
                .orElseThrow(() -> new DatosInvalidosException("El pedido no existe."));
    }

    @Transactional
    public Pedido cambiarEstado(Long pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.buscarPorIdConDetalles(pedidoId)
                .orElseThrow(() -> new DatosInvalidosException("El pedido no existe."));

        if (!pedido.puedeCambiarA(nuevoEstado)) {
            throw new DatosInvalidosException(
                    "Transicion no permitida: " + pedido.getEstado() + " -> " + nuevoEstado
                            + ". Estados permitidos: " + estadosPermitidos(pedido));
        }

        boolean seCancela = Constantes.ESTADO_CANCELADO.equalsIgnoreCase(nuevoEstado);
        pedido.cambiarEstado(nuevoEstado);

        if (seCancela) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.reponerStock(detalle.getCantidad());
                productoRepository.save(producto);
            }
        }
        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido {} cambio a estado {}", pedidoId, guardado.getEstado());
        return guardado;
    }

    public List<String> estadosPermitidos(Pedido pedido) {
        List<String> permitidos = new ArrayList<>();
        for (String estado : Constantes.ESTADOS_PEDIDO) {
            if (pedido.puedeCambiarA(estado)) {
                permitidos.add(estado);
            }
        }
        return permitidos;
    }

    @Transactional(readOnly = true)
    public long contarPedidos() {
        return pedidoRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarPedidosPendientes() {
        return pedidoRepository.countByEstado(Pedido.ESTADO_PENDIENTE);
    }

    @Transactional(readOnly = true)
    public double totalVentas() {
        Double total = pedidoRepository.totalVentas();
        return total == null ? 0.0 : total;
    }
}
