package com.misslumina.view;

import com.misslumina.config.Constantes;
import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.DetallePedido;
import com.misslumina.model.Pedido;
import com.misslumina.service.PedidoService;
import com.misslumina.service.SesionService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.components.EstadoBadge;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestion de pedidos: filtros por estado y fecha, consulta del detalle y
 * cambio de estado con validacion de transiciones.
 */
@Route("admin/pedidos")
public class GestionPedidosView extends BaseView implements BeforeEnterObserver {

    private static final String TODOS = "TODOS";

    private final PedidoService pedidoService;
    private final SesionService sesionService;

    private final ComboBox<String> estado = new ComboBox<>("Estado");
    private final DatePicker desde = new DatePicker("Desde");
    private final DatePicker hasta = new DatePicker("Hasta");
    private final Grid<Pedido> grid = new Grid<>(Pedido.class, false);
    private boolean construido;

    public GestionPedidosView(PedidoService pedidoService, SesionService sesionService) {
        this.pedidoService = pedidoService;
        this.sesionService = sesionService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!protegerAdministrador(event, sesionService)) {
            return;
        }
        if (!construido) {
            construir();
            construido = true;
        }
        cargar();
    }

    private void construir() {
        add(titulo("Gestion de pedidos"),
                subtitulo("Consulta, filtra y actualiza el estado de las transacciones."));

        List<String> estados = new ArrayList<>();
        estados.add(TODOS);
        estados.addAll(Constantes.ESTADOS_PEDIDO);
        estado.setItems(estados);
        estado.setValue(TODOS);

        Button filtrar = new Button("Filtrar", e -> cargar());
        filtrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button("Limpiar", e -> {
            estado.setValue(TODOS);
            desde.clear();
            hasta.clear();
            cargar();
        });
        limpiar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout barra = new HorizontalLayout(estado, desde, hasta, filtrar, limpiar);
        barra.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        barra.setWrap(true);
        add(barra);

        grid.addColumn(Pedido::getNumeroOrden).setHeader("Pedido").setAutoWidth(true);
        grid.addColumn(Pedido::getClienteNombre).setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(Pedido::getFechaHoraPedidoFormateada).setHeader("Fecha")
                .setAutoWidth(true);
        grid.addComponentColumn(p -> new EstadoBadge(p.getEstado())).setHeader("Estado")
                .setAutoWidth(true);
        grid.addColumn(Pedido::calcularUnidadesVendidas).setHeader("Unidades")
                .setAutoWidth(true);
        grid.addColumn(Pedido::getTotalFormateado).setHeader("Total").setAutoWidth(true);
        grid.addColumn(Pedido::getUuidCorto).setHeader("UUID").setAutoWidth(true);
        grid.addComponentColumn(this::botonGestionar).setHeader("Accion").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("500px");
        grid.setWidthFull();
        add(grid);
    }

    private void cargar() {
        try {
            List<Pedido> pedidos = pedidoService.listarTodos();
            String estadoSeleccionado = estado.getValue();
            LocalDate fechaDesde = desde.getValue();
            LocalDate fechaHasta = hasta.getValue();

            pedidos = pedidos.stream()
                    .filter(p -> estadoSeleccionado == null || TODOS.equals(estadoSeleccionado)
                            || p.getEstado().equalsIgnoreCase(estadoSeleccionado))
                    .filter(p -> fechaDesde == null || !p.getFechaPedido().isBefore(fechaDesde))
                    .filter(p -> fechaHasta == null || !p.getFechaPedido().isAfter(fechaHasta))
                    .toList();
            grid.setItems(pedidos);
        } catch (Exception ex) {
            UiUtils.error("No fue posible cargar los pedidos.");
        }
    }

    private Button botonGestionar(Pedido pedido) {
        Button gestionar = new Button("Gestionar", e -> abrirDetalle(pedido));
        gestionar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        return gestionar;
    }

    private void abrirDetalle(Pedido pedido) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Pedido " + pedido.getNumeroOrden());
        dialog.setWidth("820px");

        H3 datos = new H3("Datos generales");
        Span cliente = new Span("Cliente: " + pedido.getClienteNombre());
        Span correo = new Span("Correo: " + (pedido.getUsuario() == null ? "-"
                : pedido.getUsuario().getCorreo()));
        Span fecha = new Span("Fecha: " + pedido.getFechaHoraPedidoFormateada());
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
                .setHeader("Precio").setAutoWidth(true);
        detalles.addColumn(DetallePedido::getSubtotalFormateado).setHeader("Subtotal")
                .setAutoWidth(true);
        detalles.setItems(pedido.getDetalles());
        detalles.setAllRowsVisible(true);
        detalles.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        List<String> permitidos = pedidoService.estadosPermitidos(pedido);
        ComboBox<String> nuevoEstado = new ComboBox<>("Nuevo estado");
        nuevoEstado.setItems(permitidos);
        nuevoEstado.setPlaceholder(permitidos.isEmpty()
                ? "No hay transiciones disponibles" : "Seleccione el nuevo estado");
        nuevoEstado.setEnabled(!permitidos.isEmpty());
        nuevoEstado.setWidthFull();

        VerticalLayout contenido = new VerticalLayout(datos, cliente, correo, fecha, metodo,
                uuid, detalles, total, nuevoEstado);
        contenido.setPadding(false);
        dialog.add(contenido);

        Button cerrar = new Button("Cerrar", e -> dialog.close());
        cerrar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button cambiar = new Button("Cambiar estado", e -> {
            try {
                if (nuevoEstado.getValue() == null) {
                    UiUtils.error("Seleccione el nuevo estado.");
                    return;
                }
                pedidoService.cambiarEstado(pedido.getId(), nuevoEstado.getValue());
                dialog.close();
                cargar();
                UiUtils.exito("Estado del pedido actualizado.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        cambiar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cambiar.setEnabled(!permitidos.isEmpty());
        dialog.getFooter().add(cerrar, cambiar);
        dialog.open();
    }
}
