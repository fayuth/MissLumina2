package com.misslumina.view;

import com.misslumina.model.DetallePedido;
import com.misslumina.model.Pedido;
import com.misslumina.service.PedidoService;
import com.misslumina.service.SesionService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.components.EstadoBadge;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * Historial de pedidos del cliente autenticado, con el detalle de cada
 * transaccion y su UUID.
 */
@Route("historial")
public class HistorialPedidosView extends BaseView implements BeforeEnterObserver {

    private final PedidoService pedidoService;
    private final SesionService sesionService;
    private final Grid<Pedido> grid = new Grid<>(Pedido.class, false);
    private final Span vacio = new Span("Todavia no tienes pedidos registrados.");
    private boolean construido;

    public HistorialPedidosView(PedidoService pedidoService, SesionService sesionService) {
        this.pedidoService = pedidoService;
        this.sesionService = sesionService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!protegerSesion(event, sesionService)) {
            return;
        }
        if (!construido) {
            construir();
            construido = true;
        }
        cargar();
    }

    private void construir() {
        add(titulo("Mis pedidos"),
                subtitulo("Consulta el estado y el detalle de tus compras."));

        grid.addColumn(Pedido::getNumeroOrden).setHeader("Pedido").setAutoWidth(true);
        grid.addColumn(Pedido::getFechaHoraPedidoFormateada).setHeader("Fecha y hora")
                .setAutoWidth(true);
        grid.addComponentColumn(p -> new EstadoBadge(p.getEstado())).setHeader("Estado")
                .setAutoWidth(true);
        grid.addColumn(Pedido::calcularUnidadesVendidas).setHeader("Unidades")
                .setAutoWidth(true);
        grid.addColumn(Pedido::getTotalFormateado).setHeader("Total").setAutoWidth(true);
        grid.addColumn(Pedido::getUuidCorto).setHeader("UUID").setAutoWidth(true);
        grid.addComponentColumn(this::botonDetalle).setHeader("").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("480px");
        grid.setWidthFull();

        vacio.addClassName("empty-state");
        vacio.setVisible(false);

        add(grid, vacio);
    }

    private void cargar() {
        Long usuarioId = sesionService.getUsuarioActualId();
        if (usuarioId == null) {
            return;
        }
        try {
            List<Pedido> pedidos = pedidoService.listarPorUsuario(usuarioId);
            grid.setItems(pedidos);
            vacio.setVisible(pedidos.isEmpty());
        } catch (Exception ex) {
            UiUtils.error("No fue posible cargar tus pedidos.");
        }
    }

    private Button botonDetalle(Pedido pedido) {
        Button ver = new Button("Ver detalle", e -> mostrarDetalle(pedido));
        ver.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        return ver;
    }

    private void mostrarDetalle(Pedido pedido) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Pedido " + pedido.getNumeroOrden());
        dialog.setWidth("780px");

        H3 datos = new H3("Informacion de la transaccion");
        Span fecha = new Span("Fecha y hora: " + pedido.getFechaHoraPedidoFormateada());
        Span estado = new Span("Estado: " + pedido.getEstado());
        Span metodo = new Span("Metodo de pago: " + pedido.getMetodoPago());
        Span uuid = new Span("UUID: " + pedido.getIdTransaccion());
        uuid.addClassName("uuid-text");
        Span total = new Span("Total: " + pedido.getTotalFormateado());
        total.addClassName("summary-total-text");

        Grid<DetallePedido> detalles = new Grid<>(DetallePedido.class, false);
        detalles.addColumn(DetallePedido::getProductoNombre).setHeader("Producto")
                .setFlexGrow(2).setAutoWidth(true);
        detalles.addColumn(DetallePedido::getCantidad).setHeader("Cantidad")
                .setAutoWidth(true);
        detalles.addColumn(d -> FormatoUtil.moneda(d.getPrecioUnitario()))
                .setHeader("Precio unitario").setAutoWidth(true);
        detalles.addColumn(DetallePedido::getSubtotalFormateado).setHeader("Subtotal")
                .setAutoWidth(true);
        detalles.setItems(pedido.getDetalles());
        detalles.setAllRowsVisible(true);
        detalles.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        H2 resumen = new H2("Productos");
        resumen.addClassName("dialog-subtitle");
        VerticalLayout contenido = new VerticalLayout(datos, fecha, estado, metodo, uuid,
                detalles, resumen, total);
        contenido.setPadding(false);
        dialog.add(contenido);

        Button cerrar = new Button("Cerrar", e -> dialog.close());
        cerrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cerrar);
        dialog.open();
    }
}
