package com.misslumina.model.ecommerce;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.exception.StockInsuficienteException;
import com.misslumina.model.catalog.Producto;
import com.misslumina.util.FormatoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Carrito {

    public static final int MAX_UNIDADES_POR_PRODUCTO = 20;

    private final List<ItemCarrito> items = new ArrayList<>();

    public Carrito() {
    }

    public void agregarProducto(Producto producto, int cantidad) {
        if (producto == null) {
            throw new DatosInvalidosException("Debe seleccionar un producto.");
        }
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor que cero.");
        }
        if (!producto.isDisponible()) {
            throw new DatosInvalidosException("El producto " + producto.getNombre() + " no esta disponible.");
        }
        ItemCarrito existente = buscarItem(producto.getId());
        int nuevaCantidad = (existente == null ? 0 : existente.getCantidad()) + cantidad;
        if (nuevaCantidad > MAX_UNIDADES_POR_PRODUCTO) {
            throw new DatosInvalidosException(
                    "Solo se permiten " + MAX_UNIDADES_POR_PRODUCTO
                            + " unidades del mismo producto por pedido.");
        }
        if (!producto.hayStock(nuevaCantidad)) {
            throw new StockInsuficienteException(
                    "No hay stock suficiente de " + producto.getNombre()
                            + ". Disponible: " + producto.getStock());
        }
        if (existente == null) {
            items.add(new ItemCarrito(producto, cantidad));
        } else {
            existente.setCantidad(nuevaCantidad);
        }
    }

    public void actualizarCantidad(Long productoId, int cantidad) {
        ItemCarrito item = buscarItem(productoId);
        if (item == null) {
            throw new DatosInvalidosException("El producto no esta en el carrito.");
        }
        if (cantidad <= 0) {
            eliminarProducto(productoId);
            return;
        }
        if (cantidad > MAX_UNIDADES_POR_PRODUCTO) {
            throw new DatosInvalidosException(
                    "Solo se permiten " + MAX_UNIDADES_POR_PRODUCTO + " unidades por producto.");
        }
        if (!item.getProducto().hayStock(cantidad)) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Disponible: " + item.getProducto().getStock());
        }
        item.setCantidad(cantidad);
    }

    public void eliminarProducto(Long productoId) {
        items.removeIf(item -> item.getProducto() != null
                && Objects.equals(item.getProducto().getId(), productoId));
    }

    public void vaciar() {
        items.clear();
    }

    public ItemCarrito buscarItem(Long productoId) {
        for (ItemCarrito item : items) {
            if (item.getProducto() != null
                    && Objects.equals(item.getProducto().getId(), productoId)) {
                return item;
            }
        }
        return null;
    }

    public double calcularSubtotal() {
        double suma = 0.0;
        for (ItemCarrito item : items) {
            suma += item.calcularSubtotal();
        }
        return FormatoUtil.redondear(suma);
    }

    public double calcularIva() {
        return FormatoUtil.redondear(calcularSubtotal() * Pedido.IVA);
    }

    public double calcularTotal() {
        return FormatoUtil.redondear(calcularSubtotal() + calcularIva());
    }

    public double calcularAhorro() {
        double ahorro = 0.0;
        for (ItemCarrito item : items) {
            if (item.getProducto() != null) {
                ahorro += item.getProducto().calcularDescuentoUnitario() * item.getCantidad();
            }
        }
        return FormatoUtil.redondear(ahorro);
    }

    public int getCantidadTotal() {
        int total = 0;
        for (ItemCarrito item : items) {
            total += item.getCantidad();
        }
        return total;
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public List<ItemCarrito> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public String toString() {
        return "Carrito{items=" + items.size() +
                ", unidades=" + getCantidadTotal() +
                ", total=" + calcularTotal() +
                '}';
    }
}
