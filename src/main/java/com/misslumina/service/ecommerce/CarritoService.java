package com.misslumina.service.ecommerce;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.exception.StockInsuficienteException;
import com.misslumina.model.ecommerce.Carrito;
import com.misslumina.model.ecommerce.ItemCarrito;
import com.misslumina.model.catalog.Producto;
import com.misslumina.service.core.SesionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarritoService {

    private final SesionService sesionService;

    public CarritoService(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    public Carrito getCarrito() {
        return sesionService.getCarrito();
    }

    public List<ItemCarrito> getItems() {
        return getCarrito().getItems();
    }

    public boolean puedeComprar() {
        return !sesionService.esAdministrador();
    }

    public void agregar(Producto producto, int cantidad) {
        if (!puedeComprar()) {
            throw new DatosInvalidosException(
                    "Las cuentas de administracion no realizan compras.");
        }
        if (producto == null) {
            throw new DatosInvalidosException("Debe seleccionar un producto.");
        }
        if (!producto.hayStock(cantidad)) {
            throw new StockInsuficienteException(
                    "No hay stock suficiente de " + producto.getNombre()
                            + ". Disponible: " + producto.getStock());
        }
        getCarrito().agregarProducto(producto, cantidad);
    }

    public void actualizarCantidad(Long productoId, int cantidad) {
        getCarrito().actualizarCantidad(productoId, cantidad);
    }

    public void eliminar(Long productoId) {
        getCarrito().eliminarProducto(productoId);
    }

    public void vaciar() {
        getCarrito().vaciar();
    }

    public int getCantidadTotal() {
        return getCarrito().getCantidadTotal();
    }

    public boolean estaVacio() {
        return getCarrito().estaVacio();
    }

    public double calcularSubtotal() {
        return getCarrito().calcularSubtotal();
    }

    public double calcularIva() {
        return getCarrito().calcularIva();
    }

    public double calcularTotal() {
        return getCarrito().calcularTotal();
    }

    public double calcularAhorro() {
        return getCarrito().calcularAhorro();
    }
}
