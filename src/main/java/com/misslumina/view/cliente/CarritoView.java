package com.misslumina.view.cliente;

import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.ecommerce.ItemCarrito;
import com.misslumina.service.ecommerce.CarritoService;
import com.misslumina.service.core.SesionService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.BaseView;
import com.misslumina.view.auth.LoginView;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("carrito")
public class CarritoView extends BaseView {

    private final CarritoService carritoService;
    private final SesionService sesionService;

    public CarritoView(CarritoService carritoService, SesionService sesionService) {
        this.carritoService = carritoService;
        this.sesionService = sesionService;
        construir();
    }

    private void construir() {
        removeAll();
        add(titulo("Mi carrito"), subtitulo("Revisa tus productos antes de confirmar el pedido."));

        if (carritoService.estaVacio()) {
            add(crearVacio());
            UiUtils.actualizarContadorCarrito(0);
            return;
        }

        Grid<ItemCarrito> grid = new Grid<>(ItemCarrito.class, false);
        grid.addColumn(item -> item.getProducto().getNombre()).setHeader("Producto")
                .setFlexGrow(2).setAutoWidth(true);
        grid.addColumn(item -> item.getProducto().getMarca()).setHeader("Marca")
                .setAutoWidth(true);
        grid.addColumn(item -> FormatoUtil.moneda(item.getProducto().getPrecio()))
                .setHeader("Precio unitario").setAutoWidth(true);
        grid.addComponentColumn(this::selectorCantidad).setHeader("Cantidad")
                .setAutoWidth(true);
        grid.addColumn(ItemCarrito::getSubtotalFormateado).setHeader("Subtotal")
                .setAutoWidth(true);
        grid.addComponentColumn(this::botonEliminar).setHeader("").setAutoWidth(true);
        grid.setItems(carritoService.getItems());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        Div resumen = crearResumen();
        HorizontalLayout contenido = new HorizontalLayout(grid, resumen);
        contenido.setWidthFull();
        contenido.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.START);
        contenido.addClassName("cart-layout");
        contenido.setFlexGrow(1, grid);

        add(contenido);
        UiUtils.actualizarContadorCarrito(carritoService.getCantidadTotal());
    }

    private Div crearVacio() {
        Div vacio = new Div();
        vacio.addClassName("empty-cart");
        H2 titulo = new H2("Tu carrito esta vacio");
        Span texto = new Span("Agrega productos del catalogo para comenzar tu pedido.");
        Button ir = new Button("Ir al catalogo", e ->
                UI.getCurrent().navigate(CatalogoView.class));
        ir.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        vacio.add(titulo, texto, ir);
        return vacio;
    }

    private HorizontalLayout selectorCantidad(ItemCarrito item) {
        Button menos = new Button("-", e -> cambiarCantidad(item, -1));
        menos.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        Span cantidad = new Span(String.valueOf(item.getCantidad()));
        cantidad.addClassName("quantity-value");
        Button mas = new Button("+", e -> cambiarCantidad(item, 1));
        mas.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout selector = new HorizontalLayout(menos, cantidad, mas);
        selector.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        selector.setSpacing(false);
        return selector;
    }

    private Button botonEliminar(ItemCarrito item) {
        Button eliminar = new Button("Quitar", e -> {
            carritoService.eliminar(item.getProducto().getId());
            UiUtils.info(item.getProducto().getNombre() + " se quito del carrito.");
            construir();
        });
        eliminar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        eliminar.addClassName("danger-link");
        return eliminar;
    }

    private void cambiarCantidad(ItemCarrito item, int delta) {
        try {
            int nueva = item.getCantidad() + delta;
            carritoService.actualizarCantidad(item.getProducto().getId(), nueva);
            construir();
        } catch (MissLuminaException ex) {
            UiUtils.error(ex.getMessage());
        }
    }

    private Div crearResumen() {
        Div resumen = new Div();
        resumen.addClassName("cart-summary");

        H2 titulo = new H2("Resumen del pedido");
        titulo.addClassName("summary-title");
        resumen.add(titulo);

        resumen.add(filaResumen("Subtotal", FormatoUtil.moneda(carritoService.calcularSubtotal())));
        resumen.add(filaResumen("IVA (19%)", FormatoUtil.moneda(carritoService.calcularIva())));
        if (carritoService.calcularAhorro() > 0) {
            resumen.add(filaResumen("Ahorro",
                    FormatoUtil.moneda(carritoService.calcularAhorro()), "savings"));
        }

        Div total = filaResumen("Total", FormatoUtil.moneda(carritoService.calcularTotal()));
        total.addClassName("summary-total");
        resumen.add(total);

        Button continuar = new Button("Continuar compra", e -> continuarCompra());
        continuar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        continuar.setWidthFull();

        Button seguir = new Button("Seguir comprando", e ->
                UI.getCurrent().navigate(CatalogoView.class));
        seguir.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        seguir.setWidthFull();

        Button vaciar = new Button("Vaciar carrito", e -> UiUtils.confirmar(
                "Vaciar carrito", "Se eliminaran todos los productos del carrito.",
                () -> {
                    carritoService.vaciar();
                    UiUtils.info("El carrito quedo vacio.");
                    construir();
                }));
        vaciar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        vaciar.setWidthFull();

        VerticalLayout acciones = new VerticalLayout(continuar, seguir, vaciar);
        acciones.setPadding(false);
        acciones.setSpacing(true);
        resumen.add(acciones);
        return resumen;
    }

    private void continuarCompra() {
        if (!sesionService.estaAutenticado()) {
            UiUtils.advertencia("Inicia sesion para registrar tu pedido.");
            UI.getCurrent().navigate(LoginView.class);
            return;
        }
        UI.getCurrent().navigate(RegistroPedidoView.class);
    }

    private Div filaResumen(String etiqueta, String valor) {
        return filaResumen(etiqueta, valor, null);
    }

    private Div filaResumen(String etiqueta, String valor, String clase) {
        Div fila = new Div();
        fila.addClassName("summary-row");
        if (clase != null) {
            fila.addClassName(clase);
        }
        Span label = new Span(etiqueta);
        Span value = new Span(valor);
        value.addClassName("summary-value");
        fila.add(label, value);
        return fila;
    }
}
