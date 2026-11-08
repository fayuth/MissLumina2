package com.misslumina.view.cliente;

import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.catalog.Producto;
import com.misslumina.service.ecommerce.CarritoService;
import com.misslumina.service.catalog.ProductoService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.BaseView;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("producto/:productoId")
public class ProductoDetalleView extends BaseView implements BeforeEnterObserver {

    private final ProductoService productoService;
    private final CarritoService carritoService;

    public ProductoDetalleView(ProductoService productoService, CarritoService carritoService) {
        this.productoService = productoService;
        this.carritoService = carritoService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String valor = event.getRouteParameters().get("productoId").orElse(null);
        Long id;
        try {
            id = Long.valueOf(valor);
        } catch (NumberFormatException ex) {
            UiUtils.error("El producto solicitado no es valido.");
            event.forwardTo(CatalogoView.class);
            return;
        }

        try {
            Producto producto = productoService.buscarPorId(id);
            construir(producto);
        } catch (MissLuminaException ex) {
            UiUtils.error(ex.getMessage());
            event.forwardTo(CatalogoView.class);
        }
    }

    private void construir(Producto producto) {
        removeAll();
        setSpacing(true);

        Button volver = new Button("Volver al catalogo", e ->
                UI.getCurrent().navigate(CatalogoView.class));
        volver.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        add(volver);

        Image imagen = new Image(producto.getImagenUrlSegura(), producto.getNombre());
        imagen.addClassName("detail-image");
        Div imagenContenedor = new Div(imagen);
        imagenContenedor.addClassName("detail-image-wrap");

        Span marca = new Span(producto.getMarca());
        marca.addClassName("detail-brand");
        H2 nombre = new H2(producto.getNombre());
        nombre.addClassName("detail-title");
        Span tono = new Span(producto.getTono() == null ? "Sin tono" : "Tono: " + producto.getTono());
        tono.addClassName("detail-tone");

        Span descripcion = new Span(producto.getDescripcion() == null
                ? "Producto de maquillaje Miss Lumina." : producto.getDescripcion());
        descripcion.addClassName("detail-description");

        Div precios = new Div();
        precios.addClassName("detail-prices");
        Span precio = new Span(FormatoUtil.moneda(producto.getPrecio()));
        precio.addClassName("detail-price");
        precios.add(precio);
        if (producto.getPrecioAnterior() > producto.getPrecio()) {
            Span anterior = new Span(FormatoUtil.moneda(producto.getPrecioAnterior()));
            anterior.addClassName("product-old-price");
            Span descuento = new Span("-" + Math.round(producto.calcularPorcentajeDescuento()) + "%");
            descuento.addClassName("discount-badge");
            precios.add(anterior, descuento);
        }

        Span stock = new Span(producto.getStock() > 0
                ? "Disponible: " + producto.getStock() + " unidades"
                : "Producto agotado");
        stock.addClassName(producto.getStock() > 0 ? "stock-ok" : "stock-out");

        NumberField cantidad = new NumberField("Cantidad");
        cantidad.setMin(1);
        cantidad.setMax(Math.max(1, Math.min(producto.getStock(), 20)));
        cantidad.setValue(1.0);
        cantidad.setStepButtonsVisible(true);
        cantidad.setWidth("140px");
        cantidad.setEnabled(producto.getStock() > 0);

        Button agregar = new Button("Agregar al carrito", e -> {
            try {
                int unidades = cantidad.getValue() == null ? 1 : cantidad.getValue().intValue();
                carritoService.agregar(producto, unidades);
                UiUtils.actualizarContadorCarrito(carritoService.getCantidadTotal());
                UiUtils.exito(unidades + " unidad(es) de " + producto.getNombre()
                        + " se agregaron al carrito.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        agregar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        agregar.setEnabled(producto.isDisponible() && producto.getStock() > 0);

        Button irCarrito = new Button("Ver carrito", e ->
                UI.getCurrent().navigate(CarritoView.class));

        HorizontalLayout compra = new HorizontalLayout(cantidad, agregar, irCarrito);
        compra.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        compra.setWrap(true);

        Div informacion = new Div(marca, nombre, tono, descripcion, precios, stock, compra);
        informacion.addClassName("detail-info");

        Div contenido = new Div(imagenContenedor, informacion);
        contenido.addClassName("detail-layout");
        add(contenido);

        if (producto.esStockBajo() && producto.getStock() > 0) {
            Span aviso = new Span("Quedan pocas unidades de este producto.");
            aviso.addClassName("low-stock-warning");
            add(aviso);
        }
    }
}
