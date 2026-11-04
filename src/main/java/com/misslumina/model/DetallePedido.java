package com.misslumina.model;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.util.FormatoUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, columnDefinition = "numeric(12,2)")
    private double precioUnitario;

    @Column(name = "subtotal", nullable = false, columnDefinition = "numeric(12,2)")
    private double subtotal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    public DetallePedido() {
    }

    public DetallePedido(Producto producto, int cantidad) {
        this(producto, cantidad, producto == null ? 0.0 : producto.getPrecio());
    }

    public DetallePedido(Producto producto, int cantidad, double precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = calcularSubtotal(cantidad, precioUnitario);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public double calcularSubtotal(int cantidad, double precio) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad del detalle debe ser mayor que cero.");
        }
        return FormatoUtil.redondear(cantidad * precio);
    }

    public void recalcularSubtotal() {
        this.subtotal = calcularSubtotal(cantidad, precioUnitario);
    }

    public String getProductoNombre() {
        return producto == null ? "" : producto.getNombre();
    }

    public String getSubtotalFormateado() {
        return FormatoUtil.moneda(subtotal);
    }

    @Override
    public String toString() {
        return "DetallePedido{" +
                "id=" + id +
                ", producto=" + (producto == null ? "null" : producto.getNombre()) +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}
