package com.misslumina.model;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.util.FormatoUtil;

public class ItemCarrito {

    private Producto producto;
    private int cantidad;

    public ItemCarrito() {
    }

    public ItemCarrito(Producto producto, int cantidad) {
        if (producto == null) {
            throw new DatosInvalidosException("El producto del carrito no puede ser nulo.");
        }
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad del carrito debe ser mayor que cero.");
        }
        this.producto = producto;
        this.cantidad = cantidad;
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
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad del carrito debe ser mayor que cero.");
        }
        this.cantidad = cantidad;
    }

    public double calcularSubtotal() {
        return producto == null ? 0.0 : producto.calcularSubtotal(cantidad);
    }

    public double getSubtotal() {
        return calcularSubtotal();
    }

    public String getSubtotalFormateado() {
        return FormatoUtil.moneda(calcularSubtotal());
    }

    @Override
    public String toString() {
        return "ItemCarrito{" +
                "producto=" + (producto == null ? "null" : producto.getNombre()) +
                ", cantidad=" + cantidad +
                ", subtotal=" + calcularSubtotal() +
                '}';
    }
}
