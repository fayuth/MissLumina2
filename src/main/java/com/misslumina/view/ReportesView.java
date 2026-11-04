package com.misslumina.view;

import com.misslumina.service.ReporteService;
import com.misslumina.service.SesionService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.components.StatCard;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

/**
 * Reportes de ventas por rango de fechas. Los datos se agrupan en PostgreSQL
 * y se presentan en tablas Vaadin Grid.
 */
@Route("admin/reportes")
public class ReportesView extends BaseView implements BeforeEnterObserver {

    private final ReporteService reporteService;
    private final SesionService sesionService;

    private final DatePicker desde = new DatePicker("Desde");
    private final DatePicker hasta = new DatePicker("Hasta");
    private final Div indicadores = new Div();
    private final Grid<ReporteService.FilaReporte> gridProductos =
            new Grid<>(ReporteService.FilaReporte.class, false);
    private final Grid<ReporteService.FilaReporte> gridCategorias =
            new Grid<>(ReporteService.FilaReporte.class, false);
    private final Grid<ReporteService.EstadoReporte> gridEstados =
            new Grid<>(ReporteService.EstadoReporte.class, false);
    private final Grid<ReporteService.FilaReporte> gridDias =
            new Grid<>(ReporteService.FilaReporte.class, false);
    private boolean construido;

    public ReportesView(ReporteService reporteService, SesionService sesionService) {
        this.reporteService = reporteService;
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
        add(titulo("Reportes de ventas"),
                subtitulo("Analiza el comportamiento de la tienda por periodo."));

        desde.setValue(LocalDate.now().minusMonths(1));
        hasta.setValue(LocalDate.now());

        Button actualizar = new Button("Actualizar", e -> cargar());
        actualizar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout filtros = new HorizontalLayout(desde, hasta, actualizar);
        filtros.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        filtros.setWrap(true);
        add(filtros);

        indicadores.addClassName("stats-grid");
        indicadores.setWidthFull();
        add(indicadores);

        configurarGridProductos();
        configurarGridCategorias();
        configurarGridEstados();
        configurarGridDias();

        add(crearPanel("Productos mas vendidos", gridProductos));
        add(crearPanel("Ventas por categoria", gridCategorias));
        add(crearPanel("Pedidos por estado", gridEstados));
        add(crearPanel("Ventas por dia", gridDias));
    }

    private void configurarGridProductos() {
        gridProductos.addColumn(ReporteService.FilaReporte::etiqueta).setHeader("Producto")
                .setFlexGrow(2).setAutoWidth(true);
        gridProductos.addColumn(ReporteService.FilaReporte::unidades).setHeader("Unidades")
                .setAutoWidth(true);
        gridProductos.addColumn(f -> FormatoUtil.moneda(f.valor())).setHeader("Ingresos")
                .setAutoWidth(true);
        gridProductos.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_BORDER);
        gridProductos.setHeight("300px");
    }

    private void configurarGridCategorias() {
        gridCategorias.addColumn(ReporteService.FilaReporte::etiqueta).setHeader("Categoria")
                .setFlexGrow(2).setAutoWidth(true);
        gridCategorias.addColumn(ReporteService.FilaReporte::unidades).setHeader("Unidades")
                .setAutoWidth(true);
        gridCategorias.addColumn(f -> FormatoUtil.moneda(f.valor())).setHeader("Ingresos")
                .setAutoWidth(true);
        gridCategorias.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_BORDER);
        gridCategorias.setHeight("260px");
    }

    private void configurarGridEstados() {
        gridEstados.addColumn(ReporteService.EstadoReporte::estado).setHeader("Estado")
                .setAutoWidth(true);
        gridEstados.addColumn(ReporteService.EstadoReporte::pedidos).setHeader("Pedidos")
                .setAutoWidth(true);
        gridEstados.addColumn(e -> FormatoUtil.moneda(e.valor())).setHeader("Valor")
                .setAutoWidth(true);
        gridEstados.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        gridEstados.setHeight("260px");
    }

    private void configurarGridDias() {
        gridDias.addColumn(ReporteService.FilaReporte::etiqueta).setHeader("Fecha")
                .setAutoWidth(true);
        gridDias.addColumn(ReporteService.FilaReporte::unidades).setHeader("Unidades")
                .setAutoWidth(true);
        gridDias.addColumn(f -> FormatoUtil.moneda(f.valor())).setHeader("Ventas")
                .setAutoWidth(true);
        gridDias.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        gridDias.setHeight("280px");
    }

    private Div crearPanel(String titulo, Grid<?> grid) {
        Div panel = new Div();
        panel.addClassName("ml-card");
        H2 h2 = new H2(titulo);
        h2.addClassName("section-title");
        grid.setWidthFull();
        panel.add(h2, grid);
        return panel;
    }

    private void cargar() {
        LocalDate fechaDesde = desde.getValue();
        LocalDate fechaHasta = hasta.getValue();
        if (fechaDesde == null || fechaHasta == null) {
            UiUtils.error("Debe seleccionar las dos fechas del reporte.");
            return;
        }
        if (fechaDesde.isAfter(fechaHasta)) {
            UiUtils.error("La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        try {
            ReporteService.Resumen resumen = reporteService.resumen(fechaDesde, fechaHasta);
            indicadores.removeAll();
            FlexLayout tarjetas = new FlexLayout();
            tarjetas.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            tarjetas.addClassName("stats-grid");
            tarjetas.add(
                    new StatCard("Ventas", FormatoUtil.moneda(resumen.ventas()),
                            "En el periodo", VaadinIcon.MONEY),
                    new StatCard("Pedidos", String.valueOf(resumen.pedidos()),
                            "Ventas validas", VaadinIcon.CART),
                    new StatCard("Unidades", String.valueOf(resumen.unidades()),
                            "Productos vendidos", VaadinIcon.PACKAGE),
                    new StatCard("Ticket promedio", FormatoUtil.moneda(resumen.ticketPromedio()),
                            "Por pedido", VaadinIcon.CHART)
            );
            indicadores.add(tarjetas);

            gridProductos.setItems(reporteService.productosMasVendidos(fechaDesde, fechaHasta));
            gridCategorias.setItems(reporteService.ventasPorCategoria(fechaDesde, fechaHasta));
            gridEstados.setItems(reporteService.ventasPorEstado(fechaDesde, fechaHasta));
            gridDias.setItems(reporteService.ventasPorDia(fechaDesde, fechaHasta));
        } catch (Exception ex) {
            UiUtils.error("No fue posible generar los reportes.");
        }
    }
}
