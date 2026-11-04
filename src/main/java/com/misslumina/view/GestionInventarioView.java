package com.misslumina.view;

import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.Producto;
import com.misslumina.service.ProductoService;
import com.misslumina.service.SesionService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.components.StatCard;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * Control de inventario: consulta de unidades, valor del inventario y
 * ajuste manual de existencias.
 */
@Route("admin/inventario")
public class GestionInventarioView extends BaseView implements BeforeEnterObserver {

    private final ProductoService productoService;
    private final SesionService sesionService;

    private final TextField filtro = new TextField("Buscar");
    private final Checkbox soloStockBajo = new Checkbox("Solo stock bajo");
    private final Div indicadores = new Div();
    private final Grid<Producto> grid = new Grid<>(Producto.class, false);
    private boolean construido;

    public GestionInventarioView(ProductoService productoService, SesionService sesionService) {
        this.productoService = productoService;
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
        add(titulo("Inventario"),
                subtitulo("Controla las existencias y detecta productos por agotarse."));

        indicadores.addClassName("stats-grid");
        indicadores.setWidthFull();
        add(indicadores);

        filtro.setPlaceholder("Nombre, marca o categoria");
        filtro.setClearButtonVisible(true);
        filtro.setWidth("280px");
        soloStockBajo.addValueChangeListener(e -> cargar());
        Button buscar = new Button("Buscar", e -> cargar());
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout barra = new HorizontalLayout(filtro, buscar, soloStockBajo);
        barra.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        barra.setWrap(true);
        add(barra);

        grid.addColumn(Producto::getNombre).setHeader("Producto").setFlexGrow(2)
                .setAutoWidth(true);
        grid.addColumn(Producto::getMarca).setHeader("Marca").setAutoWidth(true);
        grid.addComponentColumn(this::stockBadge).setHeader("Stock actual")
                .setAutoWidth(true);
        grid.addColumn(p -> Producto.STOCK_MINIMO + " unidades").setHeader("Minimo")
                .setAutoWidth(true);
        grid.addColumn(p -> FormatoUtil.moneda(p.calcularValorInventario()))
                .setHeader("Valor inventario").setAutoWidth(true);
        grid.addComponentColumn(this::botonAjustar).setHeader("Accion").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("440px");
        grid.setWidthFull();
        add(grid);
    }

    private void cargar() {
        try {
            List<Producto> productos = productoService.buscar(filtro.getValue(), null, null, null);
            if (soloStockBajo.getValue()) {
                productos = productos.stream()
                        .filter(p -> p.getStock() <= Producto.STOCK_MINIMO)
                        .toList();
            }
            grid.setItems(productos);

            indicadores.removeAll();
            indicadores.add(
                    new StatCard("Unidades", String.valueOf(productoService.unidadesInventario()),
                            "En todas las bodegas", VaadinIcon.PACKAGE),
                    new StatCard("Valor inventario",
                            FormatoUtil.moneda(productoService.valorInventario()),
                            "Precio de venta actual", VaadinIcon.MONEY),
                    new StatCard("Stock bajo", String.valueOf(productoService.contarStockBajo()),
                            "Minimo " + Producto.STOCK_MINIMO + " unidades",
                            VaadinIcon.WARNING)
            );
        } catch (Exception ex) {
            UiUtils.error("No fue posible cargar el inventario.");
        }
    }

    private Span stockBadge(Producto producto) {
        Span span = new Span(producto.getStock() + " unidades");
        span.addClassName(producto.getStock() <= Producto.STOCK_MINIMO
                ? "stock-out" : "stock-ok");
        return span;
    }

    private Button botonAjustar(Producto producto) {
        Button ajustar = new Button("Ajustar", e -> abrirAjuste(producto));
        ajustar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        return ajustar;
    }

    private void abrirAjuste(Producto producto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajustar stock");
        dialog.setWidth("420px");

        Span nombre = new Span(producto.getNombre());
        nombre.addClassName("dialog-product");
        NumberField stock = new NumberField("Nuevo stock");
        stock.setMin(0);
        stock.setValue((double) producto.getStock());
        stock.setWidthFull();

        dialog.add(new Div(nombre), stock);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button guardar = new Button("Guardar", e -> {
            try {
                if (stock.getValue() == null) {
                    UiUtils.error("Debe indicar el nuevo stock.");
                    return;
                }
                productoService.actualizarStock(producto.getId(), stock.getValue().intValue());
                dialog.close();
                cargar();
                UiUtils.exito("Stock actualizado.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }
}
