package com.misslumina.view.components;

import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.catalog.Producto;
import com.misslumina.service.ecommerce.CarritoService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

public class ProductoCard extends Div {

    private final Producto producto;

    public ProductoCard(Producto producto, CarritoService carritoService) {
        this.producto = producto;
        addClassName("product-card");

        Image imagen = new Image(producto.getImagenUrlSegura(), producto.getNombre());
        imagen.addClassName("product-image");

        Div cuerpo = new Div();
        cuerpo.addClassName("product-body");

        Span marca = new Span(producto.getMarca());
        marca.addClassName("product-brand");
        Span nombre = new Span(producto.getNombre());
        nombre.addClassName("product-name");
        Span descripcion = new Span(producto.getDescripcionCorta());
        descripcion.addClassName("product-description");
        Span tono = new Span(producto.getTono() == null ? "" : "Tono: " + producto.getTono());
        tono.addClassName("product-tone");

        Div precios = new Div();
        precios.addClassName("product-prices");
        Span precio = new Span(FormatoUtil.moneda(producto.getPrecio()));
        precio.addClassName("product-price");
        precios.add(precio);
        if (producto.getPrecioAnterior() > producto.getPrecio()) {
            Span anterior = new Span(FormatoUtil.moneda(producto.getPrecioAnterior()));
            anterior.addClassName("product-old-price");
            precios.add(anterior);
        }

        Span stock = new Span(producto.getStock() > 0
                ? producto.getStock() + " disponibles" : "Agotado");
        stock.addClassName(producto.getStock() > 0 ? "stock-ok" : "stock-out");

        Button ver = new Button("Ver detalle", e ->
                UI.getCurrent().navigate("producto/" + producto.getId()));
        ver.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        Button agregar = new Button("Agregar", e -> {
            try {
                carritoService.agregar(producto, 1);
                UiUtils.actualizarContadorCarrito(carritoService.getCantidadTotal());
                UiUtils.exito(producto.getNombre() + " se agrego al carrito.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        agregar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        agregar.setEnabled(producto.isDisponible() && producto.getStock() > 0);
        agregar.setVisible(carritoService.puedeComprar());

        Div acciones = new Div(ver, agregar);
        acciones.addClassName("product-actions");

        cuerpo.add(marca, nombre, descripcion, tono, precios, stock, acciones);
        add(imagen, cuerpo);
    }

    public Producto getProducto() {
        return producto;
    }
}
