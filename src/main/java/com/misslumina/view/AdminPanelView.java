package com.misslumina.view;

import com.misslumina.model.Pedido;
import com.misslumina.service.PedidoService;
import com.misslumina.service.ReporteService;
import com.misslumina.service.SesionService;
import com.misslumina.view.components.EstadoBadge;
import com.misslumina.view.components.StatCard;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("admin")
public class AdminPanelView extends BaseView implements BeforeEnterObserver {

    private final SesionService sesionService;
    private final ReporteService reporteService;
    private final PedidoService pedidoService;

    public AdminPanelView(SesionService sesionService, ReporteService reporteService,
                          PedidoService pedidoService) {
        this.sesionService = sesionService;
        this.reporteService = reporteService;
        this.pedidoService = pedidoService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!protegerAdministrador(event, sesionService)) {
            return;
        }
        removeAll();
        construir();
    }

    private void construir() {
        add(titulo("Panel administrativo"),
                subtitulo("Resumen general de Miss Lumina."));

        ReporteService.Resumen resumen = reporteService.resumen();

        FlexLayout indicadores = new FlexLayout();
        indicadores.addClassName("stats-grid");
        indicadores.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        indicadores.add(
                new StatCard("Ventas", com.misslumina.util.FormatoUtil.moneda(resumen.ventas()),
                        resumen.pedidos() + " pedidos validos", VaadinIcon.MONEY),
                new StatCard("Pedidos pendientes", String.valueOf(resumen.pendientes()),
                        "Requieren gestion", VaadinIcon.CLOCK),
                new StatCard("Unidades vendidas", String.valueOf(resumen.unidades()),
                        "Productos despachados", VaadinIcon.PACKAGE),
                new StatCard("Clientes", String.valueOf(resumen.clientes()),
                        "Cuentas registradas", VaadinIcon.USERS),
                new StatCard("Productos", String.valueOf(resumen.productos()),
                        "En el catalogo", VaadinIcon.SHOP),
                new StatCard("Stock bajo", String.valueOf(resumen.stockBajo()),
                        "Revisar inventario", VaadinIcon.WARNING)
        );
        add(indicadores);

        add(crearAccesos());
        add(crearUltimosPedidos());
    }

    private Div crearAccesos() {
        Div panel = new Div();
        panel.addClassName("ml-card");
        H2 titulo = new H2("Accesos rapidos");
        titulo.addClassName("section-title");

        Button productos = new Button("Gestionar productos", e ->
                UI.getCurrent().navigate(GestionProductosView.class));
        productos.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button inventario = new Button("Inventario", e ->
                UI.getCurrent().navigate(GestionInventarioView.class));
        inventario.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        Button pedidos = new Button("Gestionar pedidos", e ->
                UI.getCurrent().navigate(GestionPedidosView.class));
        pedidos.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        Button catalogos = new Button("Categorias y marcas", e ->
                UI.getCurrent().navigate(GestionCategoriasMarcasView.class));
        catalogos.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button reportes = new Button("Ver reportes", e ->
                UI.getCurrent().navigate(ReportesView.class));
        reportes.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout acciones = new HorizontalLayout(productos, inventario, pedidos,
                catalogos, reportes);
        acciones.setWrap(true);
        panel.add(titulo, acciones);
        return panel;
    }

    private Div crearUltimosPedidos() {
        Div panel = new Div();
        panel.addClassName("ml-card");
        H2 titulo = new H2("Ultimos pedidos");
        titulo.addClassName("section-title");

        Grid<Pedido> grid = new Grid<>(Pedido.class, false);
        grid.addColumn(Pedido::getNumeroOrden).setHeader("Pedido").setAutoWidth(true);
        grid.addColumn(Pedido::getClienteNombre).setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(Pedido::getFechaHoraPedidoFormateada).setHeader("Fecha")
                .setAutoWidth(true);
        grid.addComponentColumn(p -> new EstadoBadge(p.getEstado())).setHeader("Estado")
                .setAutoWidth(true);
        grid.addColumn(Pedido::getTotalFormateado).setHeader("Total").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);

        try {
            List<Pedido> pedidos = pedidoService.listarTodos().stream().limit(8).toList();
            grid.setItems(pedidos);
            if (pedidos.isEmpty()) {
                Span vacio = new Span("Todavia no hay pedidos registrados.");
                vacio.addClassName("empty-state");
                panel.add(titulo, vacio);
                return panel;
            }
        } catch (Exception ex) {
            UiUtils.error("No fue posible cargar los ultimos pedidos.");
        }
        panel.add(titulo, grid);
        return panel;
    }
}
