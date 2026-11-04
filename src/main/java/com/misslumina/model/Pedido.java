package com.misslumina.model;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.exception.StockInsuficienteException;
import com.misslumina.util.FormatoUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Pedido realizado por un cliente.
 *
 * <p>Aspectos clave para la sustentacion:</p>
 * <ul>
 *   <li>{@link #generarIdTransaccion()} usa {@link UUID#randomUUID()} para
 *       crear un identificador unico e irrepetible por transaccion.</li>
 *   <li>{@link LocalDate} guarda la fecha del pedido y {@link LocalDateTime}
 *       la fecha y hora exacta.</li>
 *   <li>La relacion 1 a muchos con {@link DetallePedido} usa
 *       {@code cascade = CascadeType.ALL} y {@code orphanRemoval = true}.</li>
 *   <li>{@link #calcularTotal()} suma subtotales y aplica el IVA.</li>
 * </ul>
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    public static final double IVA = 0.19;

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_CONFIRMADO = "CONFIRMADO";
    public static final String ESTADO_ENVIADO = "ENVIADO";
    public static final String ESTADO_ENTREGADO = "ENTREGADO";
    public static final String ESTADO_CANCELADO = "CANCELADO";

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_transaccion", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID idTransaccion;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;

    @Column(name = "fecha_hora_pedido", nullable = false)
    private LocalDateTime fechaHoraPedido;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "total", nullable = false, columnDefinition = "numeric(12,2)")
    private double total;

    @Column(name = "metodo_pago", nullable = false, length = 40)
    private String metodoPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
        this.estado = ESTADO_PENDIENTE;
        this.detalles = new ArrayList<>();
    }

    public Pedido(Usuario usuario, String metodoPago) {
        this();
        this.usuario = usuario;
        this.metodoPago = metodoPago;
        this.fechaPedido = LocalDate.now();
        this.fechaHoraPedido = LocalDateTime.now();
        this.idTransaccion = generarIdTransaccion();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(UUID idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public LocalDate getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDate fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public LocalDateTime getFechaHoraPedido() {
        return fechaHoraPedido;
    }

    public void setFechaHoraPedido(LocalDateTime fechaHoraPedido) {
        this.fechaHoraPedido = fechaHoraPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles == null ? new ArrayList<>() : new ArrayList<>(detalles);
    }

    /** Funcion estatica: genera el UUID unico de la transaccion. */
    public static UUID generarIdTransaccion() {
        return UUID.randomUUID();
    }

    /**
     * Procedimiento: inicializa los datos de la transaccion.
     * Genera UUID, fecha, fecha-hora y estado inicial si aun no existen.
     */
    public void generarTransaccion() {
        if (this.idTransaccion == null) {
            this.idTransaccion = generarIdTransaccion();
        }
        if (this.fechaPedido == null) {
            this.fechaPedido = LocalDate.now();
        }
        if (this.fechaHoraPedido == null) {
            this.fechaHoraPedido = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = ESTADO_PENDIENTE;
        }
    }

    /**
     * Procedimiento de registro: valida, genera la transaccion y recalcula.
     * Es el metodo que se puede mostrar como "RegistrarPedido()" en la
     * sustentacion.
     */
    public void registrarPedido() {
        if (usuario == null) {
            throw new DatosInvalidosException("El pedido debe tener un usuario.");
        }
        if (detalles.isEmpty()) {
            throw new DatosInvalidosException("El pedido debe tener al menos un producto.");
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            throw new DatosInvalidosException("Debe seleccionar un metodo de pago.");
        }
        generarTransaccion();
        recalcularTotal();
    }

    /**
     * Procedimiento: agrega un detalle validando el stock del producto.
     * No guarda en base de datos; eso lo hace el servicio dentro de una
     * transaccion.
     */
    public void agregarDetalle(Producto producto, int cantidad) {
        if (producto == null) {
            throw new DatosInvalidosException("El producto del detalle no puede ser nulo.");
        }
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad del producto debe ser mayor que cero.");
        }
        if (!producto.hayStock(cantidad)) {
            throw new StockInsuficienteException(
                    "No hay stock suficiente de " + producto.getNombre()
                            + ". Disponible: " + producto.getStock());
        }
        DetallePedido detalle = new DetallePedido(producto, cantidad);
        detalle.setPedido(this);
        this.detalles.add(detalle);
        recalcularTotal();
    }

    /** Funcion: suma los subtotales de todos los detalles. */
    public double calcularSubtotal() {
        double suma = 0.0;
        for (DetallePedido detalle : detalles) {
            suma += detalle.getSubtotal();
        }
        return FormatoUtil.redondear(suma);
    }

    /** Funcion: suma los descuentos de los productos del pedido. */
    public double calcularDescuentoTotal() {
        double descuento = 0.0;
        for (DetallePedido detalle : detalles) {
            if (detalle.getProducto() != null) {
                descuento += detalle.getProducto().calcularDescuentoUnitario()
                        * detalle.getCantidad();
            }
        }
        return FormatoUtil.redondear(descuento);
    }

    /** Funcion: calcula el IVA sobre el subtotal. */
    public double calcularIva() {
        return FormatoUtil.redondear(calcularSubtotal() * IVA);
    }

    /** Funcion: total del pedido = subtotal + IVA. */
    public double calcularTotal() {
        return FormatoUtil.redondear(calcularSubtotal() + calcularIva());
    }

    /** Procedimiento: actualiza el atributo total con el valor calculado. */
    public void recalcularTotal() {
        this.total = calcularTotal();
    }

    /** Funcion: unidades vendidas en el pedido. */
    public int calcularUnidadesVendidas() {
        int unidades = 0;
        for (DetallePedido detalle : detalles) {
            unidades += detalle.getCantidad();
        }
        return unidades;
    }

    /** Funcion: valida si el pedido puede pasar al estado indicado. */
    public boolean puedeCambiarA(String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.equalsIgnoreCase(this.estado)) {
            return false;
        }
        String destino = nuevoEstado.toUpperCase();
        return switch (this.estado == null ? "" : this.estado.toUpperCase()) {
            case ESTADO_PENDIENTE ->
                    destino.equals(ESTADO_CONFIRMADO) || destino.equals(ESTADO_CANCELADO);
            case ESTADO_CONFIRMADO ->
                    destino.equals(ESTADO_ENVIADO) || destino.equals(ESTADO_CANCELADO);
            case ESTADO_ENVIADO -> destino.equals(ESTADO_ENTREGADO);
            default -> false;
        };
    }

    /** Procedimiento: cambia el estado validando la transicion. */
    public void cambiarEstado(String nuevoEstado) {
        if (!puedeCambiarA(nuevoEstado)) {
            throw new DatosInvalidosException(
                    "No se puede cambiar el pedido de " + estado + " a " + nuevoEstado + ".");
        }
        this.estado = nuevoEstado.toUpperCase();
    }

    public boolean estaCancelado() {
        return ESTADO_CANCELADO.equalsIgnoreCase(estado);
    }

    public String getFechaPedidoFormateada() {
        return fechaPedido == null ? "" : fechaPedido.format(FORMATO_FECHA);
    }

    public String getFechaHoraPedidoFormateada() {
        return fechaHoraPedido == null ? "" : fechaHoraPedido.format(FORMATO_FECHA_HORA);
    }

    public String getTotalFormateado() {
        return FormatoUtil.moneda(total);
    }

    public String getNumeroOrden() {
        return id == null ? "ML-NUEVO" : String.format("ML-%06d", id);
    }

    public String getUuidCorto() {
        return idTransaccion == null ? "" : idTransaccion.toString().substring(0, 8).toUpperCase();
    }

    public String getClienteNombre() {
        return usuario == null ? "" : usuario.getNombre();
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", idTransaccion=" + idTransaccion +
                ", fechaPedido=" + fechaPedido +
                ", fechaHoraPedido=" + fechaHoraPedido +
                ", estado='" + estado + '\'' +
                ", total=" + total +
                ", metodoPago='" + metodoPago + '\'' +
                ", unidades=" + calcularUnidadesVendidas() +
                '}';
    }
}
