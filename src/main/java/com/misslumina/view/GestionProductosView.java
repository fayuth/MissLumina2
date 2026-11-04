package com.misslumina.view;

import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.Producto;
import com.misslumina.service.ProductoService;
import com.misslumina.service.SesionService;
import com.misslumina.util.FormatoUtil;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * CRUD de productos. El administrador puede crear, editar, activar,
 * desactivar y eliminar productos sin ventas asociadas.
 */
@Route("admin/productos")
public class GestionProductosView extends BaseView implements BeforeEnterObserver {

    private final ProductoService productoService;
    private final SesionService sesionService;

    private final TextField filtro = new TextField("Buscar producto");
    private final Grid<Producto> grid = new Grid<>(Producto.class, false);
    private boolean construido;

    public GestionProductosView(ProductoService productoService, SesionService sesionService) {
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
        add(encabezado(titulo("Gestion de productos"),
                new Button("Nuevo producto", e -> abrirFormulario(null))));
        add(subtitulo("Administra el catalogo, precios, tonos y disponibilidad."));

        filtro.setPlaceholder("Nombre, marca o categoria");
        filtro.setClearButtonVisible(true);
        filtro.setWidth("300px");
        Button buscar = new Button("Buscar", e -> cargar());
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout barra = new HorizontalLayout(filtro, buscar);
        barra.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        add(barra);

        grid.addColumn(Producto::getNombre).setHeader("Producto").setFlexGrow(2)
                .setAutoWidth(true);
        grid.addColumn(Producto::getMarca).setHeader("Marca").setAutoWidth(true);
        grid.addColumn(Producto::getCategoria).setHeader("Categoria").setAutoWidth(true);
        grid.addColumn(p -> p.getTono() == null ? "-" : p.getTono()).setHeader("Tono")
                .setAutoWidth(true);
        grid.addColumn(p -> FormatoUtil.moneda(p.getPrecio())).setHeader("Precio")
                .setAutoWidth(true);
        grid.addComponentColumn(this::stockBadge).setHeader("Stock").setAutoWidth(true);
        grid.addComponentColumn(p -> new Span(p.isDisponible() ? "Activo" : "Inactivo"))
                .setHeader("Estado").setAutoWidth(true);
        grid.addComponentColumn(this::acciones).setHeader("Acciones").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("520px");
        grid.setWidthFull();
        add(grid);
    }

    private Span stockBadge(Producto producto) {
        Span span = new Span(producto.getStock() + " unidades");
        span.addClassName(producto.getStock() <= Producto.STOCK_MINIMO
                ? "stock-out" : "stock-ok");
        return span;
    }

    private HorizontalLayout acciones(Producto producto) {
        Button editar = new Button("Editar", e -> abrirFormulario(producto));
        editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        Button disponibilidad = new Button(producto.isDisponible() ? "Desactivar" : "Activar",
                e -> cambiarDisponibilidad(producto));
        disponibilidad.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        Button eliminar = new Button("Eliminar", e -> UiUtils.confirmar(
                "Eliminar producto",
                "Se eliminara " + producto.getNombre() + ". Esta accion no se puede deshacer.",
                () -> eliminar(producto)));
        eliminar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_SMALL);

        HorizontalLayout acciones = new HorizontalLayout(editar, disponibilidad, eliminar);
        acciones.setSpacing(false);
        return acciones;
    }

    private void cargar() {
        try {
            grid.setItems(productoService.buscar(filtro.getValue(), null, null, null));
        } catch (Exception ex) {
            UiUtils.error("No fue posible cargar los productos.");
        }
    }

    private void abrirFormulario(Producto existente) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existente == null ? "Nuevo producto" : "Editar producto");
        dialog.setWidth("680px");

        TextField nombre = new TextField("Nombre");
        nombre.setWidthFull();
        nombre.setValue(existente == null ? "" : existente.getNombre());
        TextArea descripcion = new TextArea("Descripcion");
        descripcion.setWidthFull();
        descripcion.setValue(existente == null || existente.getDescripcion() == null
                ? "" : existente.getDescripcion());

        NumberField precio = new NumberField("Precio actual");
        precio.setMin(0);
        precio.setValue(existente == null ? 0.0 : existente.getPrecio());
        NumberField precioAnterior = new NumberField("Precio anterior (opcional)");
        precioAnterior.setMin(0);
        precioAnterior.setValue(existente == null ? 0.0 : existente.getPrecioAnterior());
        NumberField stock = new NumberField("Stock");
        stock.setMin(0);
        stock.setValue(existente == null ? 0.0 : (double) existente.getStock());

        ComboBox<String> marca = new ComboBox<>("Marca");
        marca.setItems(productoService.listarMarcas());
        marca.setValue(existente == null ? null : existente.getMarca());
        marca.setWidthFull();
        ComboBox<String> categoria = new ComboBox<>("Categoria");
        categoria.setItems(productoService.listarCategorias());
        categoria.setValue(existente == null ? null : existente.getCategoria());
        categoria.setWidthFull();
        ComboBox<String> tono = new ComboBox<>("Tono (opcional)");
        tono.setItems(productoService.listarTonos());
        tono.setValue(existente == null ? null : existente.getTono());
        tono.setClearButtonVisible(true);
        tono.setWidthFull();

        Checkbox disponible = new Checkbox("Disponible para la venta");
        disponible.setValue(existente == null || existente.isDisponible());

        FormLayout formulario = new FormLayout(nombre, descripcion, precio, precioAnterior,
                stock, marca, categoria, tono, disponible);
        formulario.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("560px", 2));
        formulario.setColspan(nombre, 2);
        formulario.setColspan(descripcion, 2);

        dialog.add(formulario);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button guardar = new Button("Guardar", e -> {
            try {
                Producto producto = existente == null ? new Producto() : existente;
                producto.setNombre(nombre.getValue());
                producto.setDescripcion(descripcion.getValue());
                producto.setPrecio(precio.getValue() == null ? 0.0 : precio.getValue());
                producto.setPrecioAnterior(precioAnterior.getValue() == null
                        ? 0.0 : precioAnterior.getValue());
                producto.setStock(stock.getValue() == null ? 0 : stock.getValue().intValue());
                producto.setMarca(marca.getValue());
                producto.setCategoria(categoria.getValue());
                producto.setTono(tono.getValue());
                producto.setDisponible(disponible.getValue());
                productoService.guardar(producto);
                dialog.close();
                cargar();
                UiUtils.exito("Producto guardado correctamente.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            } catch (Exception ex) {
                UiUtils.error("No fue posible guardar el producto.");
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }

    private void cambiarDisponibilidad(Producto producto) {
        try {
            productoService.cambiarDisponibilidad(producto.getId(), !producto.isDisponible());
            cargar();
            UiUtils.exito("Disponibilidad actualizada.");
        } catch (MissLuminaException ex) {
            UiUtils.error(ex.getMessage());
        }
    }

    private void eliminar(Producto producto) {
        try {
            productoService.eliminar(producto.getId());
            cargar();
            UiUtils.exito("Producto eliminado.");
        } catch (MissLuminaException ex) {
            UiUtils.error(ex.getMessage());
        }
    }
}
