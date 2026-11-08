package com.misslumina.view.cliente;

import com.misslumina.config.Constantes;
import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.ecommerce.ItemCarrito;
import com.misslumina.model.ecommerce.Pedido;
import com.misslumina.model.core.Usuario;
import com.misslumina.service.ecommerce.CarritoService;
import com.misslumina.service.ecommerce.PedidoService;
import com.misslumina.service.core.SesionService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.BaseView;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("checkout")
public class RegistroPedidoView extends BaseView implements BeforeEnterObserver {

    private final SesionService sesionService;
    private final CarritoService carritoService;
    private final PedidoService pedidoService;

    public RegistroPedidoView(SesionService sesionService, CarritoService carritoService,
                              PedidoService pedidoService) {
        this.sesionService = sesionService;
        this.carritoService = carritoService;
        this.pedidoService = pedidoService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!protegerSesion(event, sesionService)) {
            return;
        }
        if (carritoService.estaVacio()) {
            UiUtils.advertencia("El carrito esta vacio. Agrega productos antes de continuar.");
            event.forwardTo(CarritoView.class);
            return;
        }
        construir();
    }

    private void construir() {
        removeAll();
        add(titulo("Confirmar pedido"),
                subtitulo("Verifica los datos y elige tu metodo de pago."));

        Div resumen = new Div();
        resumen.addClassName("checkout-summary");
        H2 tituloResumen = new H2("Productos del pedido");
        tituloResumen.addClassName("summary-title");
        resumen.add(tituloResumen);

        for (ItemCarrito item : carritoService.getItems()) {
            Div linea = new Div();
            linea.addClassName("checkout-line");
            Span nombre = new Span(item.getCantidad() + " x " + item.getProducto().getNombre());
            Span subtotal = new Span(item.getSubtotalFormateado());
            subtotal.addClassName("summary-value");
            linea.add(nombre, subtotal);
            resumen.add(linea);
        }

        resumen.add(fila("Subtotal", FormatoUtil.moneda(carritoService.calcularSubtotal())));
        resumen.add(fila("IVA (19%)", FormatoUtil.moneda(carritoService.calcularIva())));
        Div total = fila("Total a pagar", FormatoUtil.moneda(carritoService.calcularTotal()));
        total.addClassName("summary-total");
        resumen.add(total);

        Usuario usuario = sesionService.getUsuarioActual();
        Div datos = new Div();
        datos.addClassName("checkout-data");
        H3 tituloDatos = new H3("Datos del cliente");
        Span nombreCliente = new Span(usuario == null ? "" : usuario.getNombre());
        Span correo = new Span(usuario == null ? "" : usuario.getCorreo());
        Span telefono = new Span(usuario == null || usuario.getTelefono() == null
                ? "Sin telefono registrado" : usuario.getTelefono());
        datos.add(tituloDatos, nombreCliente, correo, telefono);

        ComboBox<String> metodoPago = new ComboBox<>("Metodo de pago");
        metodoPago.setItems(Constantes.METODOS_PAGO);
        metodoPago.setValue(Constantes.METODOS_PAGO.get(0));
        metodoPago.setWidthFull();
        metodoPago.setRequiredIndicatorVisible(true);

        Div pago = new Div(metodoPago);
        pago.addClassName("checkout-payment");

        Button confirmar = new Button("Confirmar y registrar pedido", e -> registrar(metodoPago));
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmar.setWidthFull();

        Button volver = new Button("Volver al carrito", e ->
                UI.getCurrent().navigate(CarritoView.class));
        volver.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout columna = new VerticalLayout(resumen, datos, pago, confirmar, volver);
        columna.setPadding(false);
        columna.setSpacing(true);
        columna.addClassName("checkout-column");

        add(columna);
    }

    private void registrar(ComboBox<String> metodoPago) {
        try {
            if (metodoPago.getValue() == null || metodoPago.getValue().isBlank()) {
                UiUtils.error("Debe seleccionar un metodo de pago.");
                return;
            }
            Pedido pedido = pedidoService.registrarPedido(
                    sesionService.getUsuarioActual(), carritoService.getCarrito(),
                    metodoPago.getValue());
            carritoService.vaciar();
            UiUtils.actualizarContadorCarrito(0);
            mostrarConfirmacion(pedido);
        } catch (MissLuminaException ex) {
            UiUtils.error(ex.getMessage());
        } catch (Exception ex) {
            UiUtils.error("No fue posible registrar el pedido. Intente nuevamente.");
        }
    }

    private void mostrarConfirmacion(Pedido pedido) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Pedido confirmado");

        Span titulo = new Span("Gracias por tu compra");
        titulo.addClassName("auth-title");
        Span numero = new Span("Pedido " + pedido.getNumeroOrden());
        numero.addClassName("success-order");
        Span uuid = new Span("Transaccion: " + pedido.getIdTransaccion());
        uuid.addClassName("success-uuid");
        Span total = new Span("Total: " + pedido.getTotalFormateado());
        total.addClassName("success-total");
        Span estado = new Span("Estado inicial: " + pedido.getEstado());

        VerticalLayout contenido = new VerticalLayout(titulo, numero, uuid, total, estado);
        dialog.add(contenido);

        Button historial = new Button("Ver mis pedidos", e -> {
            dialog.close();
            UI.getCurrent().navigate(HistorialPedidosView.class);
        });
        historial.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button seguir = new Button("Seguir comprando", e -> {
            dialog.close();
            UI.getCurrent().navigate(CatalogoView.class);
        });
        seguir.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(seguir, historial);
        dialog.open();
    }

    private Div fila(String etiqueta, String valor) {
        Div fila = new Div();
        fila.addClassName("summary-row");
        Span label = new Span(etiqueta);
        Span value = new Span(valor);
        value.addClassName("summary-value");
        fila.add(label, value);
        return fila;
    }
}
