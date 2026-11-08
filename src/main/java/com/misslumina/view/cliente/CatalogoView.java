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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("catalogo")
public class CatalogoView extends BaseView implements BeforeEnterObserver {

    private final ProductoService productoService;
    private final CarritoService carritoService;

    private final TextField buscar = new TextField("Buscar");
    private final ComboBox<String> categoria = new ComboBox<>("Categoria");
    private final ComboBox<String> marca = new ComboBox<>("Marca");
    private final ComboBox<String> tono = new ComboBox<>("Tono");
    private final Grid<Producto> grid = new Grid<>(Producto.class, false);
    private final Span resultado = new Span();

    public CatalogoView(ProductoService productoService, CarritoService carritoService) {
        this.productoService = productoService;
        this.carritoService = carritoService;
        construir();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String categoriaParam = event.getLocation().getQueryParameters()
                .getSingleParameter("categoria").orElse(null);
        if (categoriaParam != null && !categoriaParam.isBlank()) {
            categoria.setValue(categoriaParam);
        }
        cargar();
    }

    private void construir() {
        add(titulo("Catalogo"), subtitulo("Encuentra el tono perfecto para cada ocasion."));

        buscar.setPlaceholder("Nombre, marca, categoria o tono");
        buscar.setClearButtonVisible(true);
        buscar.setWidth("260px");
        categoria.setItems(productoService.listarCategorias());
        categoria.setClearButtonVisible(true);
        marca.setItems(productoService.listarMarcas());
        marca.setClearButtonVisible(true);
        tono.setItems(productoService.listarTonosUsados());
        tono.setClearButtonVisible(true);

        Button filtrar = new Button("Buscar", e -> cargar());
        filtrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button("Limpiar", e -> {
            buscar.clear();
            categoria.clear();
            marca.clear();
            tono.clear();
            cargar();
        });
        limpiar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout filtros = new HorizontalLayout(buscar, categoria, marca, tono, filtrar, limpiar);
        filtros.setWidthFull();
        filtros.setWrap(true);
        filtros.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        filtros.addClassName("filter-bar");

        grid.addComponentColumn(this::miniatura).setHeader("Imagen")
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Producto::getNombre).setHeader("Producto").setFlexGrow(2)
                .setAutoWidth(true);
        grid.addColumn(Producto::getDescripcionCorta).setHeader("Descripcion")
                .setFlexGrow(2).setAutoWidth(true);
        grid.addColumn(Producto::getMarca).setHeader("Marca").setAutoWidth(true);
        grid.addColumn(Producto::getCategoria).setHeader("Categoria").setAutoWidth(true);
        grid.addColumn(p -> p.getTono() == null ? "-" : p.getTono()).setHeader("Tono")
                .setAutoWidth(true);
        grid.addColumn(p -> FormatoUtil.moneda(p.getPrecio())).setHeader("Precio")
                .setAutoWidth(true);
        grid.addComponentColumn(this::stockBadge)
                .setHeader("Inventario").setAutoWidth(true);
        grid.addComponentColumn(this::acciones).setHeader("Acciones").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("540px");
        grid.setWidthFull();

        resultado.addClassName("result-count");

        add(filtros, resultado, grid);
    }

    private Image miniatura(Producto producto) {
        Image imagen = new Image(producto.getImagenUrlSegura(), producto.getNombre());
        imagen.addClassName("grid-product-image");
        return imagen;
    }

    private Span stockBadge(Producto producto) {
        Span span = new Span(producto.getStock() > 0
                ? producto.getStock() + " en stock" : "Agotado");
        span.addClassName(producto.getStock() > 0 ? "stock-ok" : "stock-out");
        return span;
    }

    private HorizontalLayout acciones(Producto producto) {
        Button ver = new Button("Ver", e ->
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

        HorizontalLayout acciones = new HorizontalLayout(ver, agregar);
        acciones.setSpacing(false);
        return acciones;
    }

    private void cargar() {
        try {
            List<Producto> productos = productoService.buscar(
                    buscar.getValue(), categoria.getValue(), marca.getValue(), tono.getValue());
            grid.setItems(productos);
            resultado.setText(productos.size() + " producto(s) encontrado(s)");
        } catch (Exception ex) {
            UiUtils.error("No fue posible cargar el catalogo.");
        }
    }
}
