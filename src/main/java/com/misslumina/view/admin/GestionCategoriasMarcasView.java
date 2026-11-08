package com.misslumina.view.admin;

import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.catalog.Categoria;
import com.misslumina.model.catalog.Marca;
import com.misslumina.model.catalog.Tono;
import com.misslumina.service.catalog.CategoriaService;
import com.misslumina.service.catalog.MarcaService;
import com.misslumina.service.core.SesionService;
import com.misslumina.service.ecommerce.TonoService;
import com.misslumina.view.BaseView;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("admin/catalogos")
public class GestionCategoriasMarcasView extends BaseView implements BeforeEnterObserver {

    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    private final TonoService tonoService;
    private final SesionService sesionService;

    private final Grid<Categoria> gridCategorias = new Grid<>(Categoria.class, false);
    private final Grid<Marca> gridMarcas = new Grid<>(Marca.class, false);
    private final Grid<Tono> gridTonos = new Grid<>(Tono.class, false);
    private boolean construido;

    public GestionCategoriasMarcasView(CategoriaService categoriaService,
                                       MarcaService marcaService, TonoService tonoService,
                                       SesionService sesionService) {
        this.categoriaService = categoriaService;
        this.marcaService = marcaService;
        this.tonoService = tonoService;
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
        cargarTodo();
    }

    private void construir() {
        add(titulo("Categorias, marcas y tonos"),
                subtitulo("Estas tablas alimentan los filtros y formularios del catalogo."));

        configurarGridCategorias();
        configurarGridMarcas();
        configurarGridTonos();

        add(crearPanel("Categorias", "Nueva categoria",
                e -> abrirCategoria(null), gridCategorias, 260));
        add(crearPanel("Marcas", "Nueva marca",
                e -> abrirMarca(null), gridMarcas, 260));
        add(crearPanel("Tonos", "Nuevo tono",
                e -> abrirTono(null), gridTonos, 260));
    }

    private Div crearPanel(String titulo, String textoBoton,
                           com.vaadin.flow.component.ComponentEventListener<
                                   com.vaadin.flow.component.ClickEvent<
                                           com.vaadin.flow.component.button.Button>> accion,
                           Grid<?> grid, int altura) {
        Div panel = new Div();
        panel.addClassName("ml-card");

        H2 h2 = new H2(titulo);
        h2.addClassName("section-title");
        Button nuevo = new Button(textoBoton, accion);
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout cabecera = new HorizontalLayout(h2, nuevo);
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        cabecera.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        grid.setHeight(altura + "px");
        grid.setWidthFull();
        panel.add(cabecera, grid);
        return panel;
    }

    private void configurarGridCategorias() {
        gridCategorias.addColumn(Categoria::getNombre).setHeader("Nombre").setFlexGrow(1)
                .setAutoWidth(true);
        gridCategorias.addColumn(c -> c.getDescripcion() == null ? "-" : c.getDescripcion())
                .setHeader("Descripcion").setFlexGrow(2).setAutoWidth(true);
        gridCategorias.addComponentColumn(c -> estado(c.isActiva())).setHeader("Estado")
                .setAutoWidth(true);
        gridCategorias.addComponentColumn(c -> accionesCategoria(c)).setHeader("Acciones")
                .setAutoWidth(true);
        gridCategorias.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_BORDER);
    }

    private void configurarGridMarcas() {
        gridMarcas.addColumn(Marca::getNombre).setHeader("Nombre").setFlexGrow(1)
                .setAutoWidth(true);
        gridMarcas.addColumn(m -> m.getPaisOrigen() == null ? "-" : m.getPaisOrigen())
                .setHeader("Pais de origen").setAutoWidth(true);
        gridMarcas.addComponentColumn(m -> estado(m.isActiva())).setHeader("Estado")
                .setAutoWidth(true);
        gridMarcas.addComponentColumn(this::accionesMarca).setHeader("Acciones")
                .setAutoWidth(true);
        gridMarcas.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
    }

    private void configurarGridTonos() {
        gridTonos.addColumn(Tono::getNombre).setHeader("Nombre").setFlexGrow(1)
                .setAutoWidth(true);
        gridTonos.addComponentColumn(this::muestraColor).setHeader("Color")
                .setAutoWidth(true);
        gridTonos.addComponentColumn(t -> estado(t.isActivo())).setHeader("Estado")
                .setAutoWidth(true);
        gridTonos.addComponentColumn(this::accionesTono).setHeader("Acciones")
                .setAutoWidth(true);
        gridTonos.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
    }

    private Span estado(boolean activo) {
        Span span = new Span(activo ? "Activo" : "Inactivo");
        span.addClassName(activo ? "stock-ok" : "stock-out");
        return span;
    }

    private Div muestraColor(Tono tono) {
        Div color = new Div();
        color.addClassName("tone-swatch");
        if (tono.getCodigoHex() != null && tono.getCodigoHex().matches("#[0-9a-fA-F]{6}")) {
            color.getStyle().set("background", tono.getCodigoHex());
        }
        Span codigo = new Span(tono.getCodigoHex() == null ? "-" : tono.getCodigoHex());
        HorizontalLayout fila = new HorizontalLayout(color, codigo);
        fila.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        Div contenedor = new Div(fila);
        return contenedor;
    }

    private HorizontalLayout accionesCategoria(Categoria categoria) {
        Button editar = new Button("Editar", e -> abrirCategoria(categoria));
        editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        Button eliminar = new Button("Eliminar", e -> UiUtils.confirmar(
                "Eliminar categoria", "Se eliminara la categoria " + categoria.getNombre() + ".",
                () -> eliminarCategoria(categoria)));
        eliminar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_SMALL);
        HorizontalLayout acciones = new HorizontalLayout(editar, eliminar);
        acciones.setSpacing(false);
        return acciones;
    }

    private HorizontalLayout accionesMarca(Marca marca) {
        Button editar = new Button("Editar", e -> abrirMarca(marca));
        editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        Button eliminar = new Button("Eliminar", e -> UiUtils.confirmar(
                "Eliminar marca", "Se eliminara la marca " + marca.getNombre() + ".",
                () -> eliminarMarca(marca)));
        eliminar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_SMALL);
        HorizontalLayout acciones = new HorizontalLayout(editar, eliminar);
        acciones.setSpacing(false);
        return acciones;
    }

    private HorizontalLayout accionesTono(Tono tono) {
        Button editar = new Button("Editar", e -> abrirTono(tono));
        editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        Button eliminar = new Button("Eliminar", e -> UiUtils.confirmar(
                "Eliminar tono", "Se eliminara el tono " + tono.getNombre() + ".",
                () -> eliminarTono(tono)));
        eliminar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_SMALL);
        HorizontalLayout acciones = new HorizontalLayout(editar, eliminar);
        acciones.setSpacing(false);
        return acciones;
    }

    private void abrirCategoria(Categoria existente) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existente == null ? "Nueva categoria" : "Editar categoria");
        dialog.setWidth("460px");

        TextField nombre = new TextField("Nombre");
        nombre.setWidthFull();
        nombre.setValue(existente == null ? "" : existente.getNombre());
        TextArea descripcion = new TextArea("Descripcion");
        descripcion.setWidthFull();
        descripcion.setValue(existente == null || existente.getDescripcion() == null
                ? "" : existente.getDescripcion());
        Checkbox activa = new Checkbox("Activa");
        activa.setValue(existente == null || existente.isActiva());

        dialog.add(nombre, descripcion, activa);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button guardar = new Button("Guardar", e -> {
            try {
                Categoria categoria = existente == null ? new Categoria() : existente;
                categoria.setNombre(nombre.getValue());
                categoria.setDescripcion(descripcion.getValue());
                categoria.setActiva(activa.getValue());
                categoriaService.guardar(categoria);
                dialog.close();
                cargarTodo();
                UiUtils.exito("Categoria guardada.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }

    private void abrirMarca(Marca existente) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existente == null ? "Nueva marca" : "Editar marca");
        dialog.setWidth("460px");

        TextField nombre = new TextField("Nombre");
        nombre.setWidthFull();
        nombre.setValue(existente == null ? "" : existente.getNombre());
        TextField pais = new TextField("Pais de origen");
        pais.setWidthFull();
        pais.setValue(existente == null || existente.getPaisOrigen() == null
                ? "" : existente.getPaisOrigen());
        Checkbox activa = new Checkbox("Activa");
        activa.setValue(existente == null || existente.isActiva());

        dialog.add(nombre, pais, activa);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button guardar = new Button("Guardar", e -> {
            try {
                Marca marca = existente == null ? new Marca() : existente;
                marca.setNombre(nombre.getValue());
                marca.setPaisOrigen(pais.getValue());
                marca.setActiva(activa.getValue());
                marcaService.guardar(marca);
                dialog.close();
                cargarTodo();
                UiUtils.exito("Marca guardada.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }

    private void abrirTono(Tono existente) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existente == null ? "Nuevo tono" : "Editar tono");
        dialog.setWidth("460px");

        TextField nombre = new TextField("Nombre");
        nombre.setWidthFull();
        nombre.setValue(existente == null ? "" : existente.getNombre());
        TextField codigo = new TextField("Codigo hexadecimal");
        codigo.setPlaceholder("#CCCCCC");
        codigo.setWidthFull();
        codigo.setValue(existente == null || existente.getCodigoHex() == null
                ? "#CCCCCC" : existente.getCodigoHex());
        Checkbox activo = new Checkbox("Activo");
        activo.setValue(existente == null || existente.isActivo());

        dialog.add(nombre, codigo, activo);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button guardar = new Button("Guardar", e -> {
            try {
                Tono tono = existente == null ? new Tono() : existente;
                tono.setNombre(nombre.getValue());
                tono.setCodigoHex(codigo.getValue());
                tono.setActivo(activo.getValue());
                tonoService.guardar(tono);
                dialog.close();
                cargarTodo();
                UiUtils.exito("Tono guardado.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }

    private void eliminarCategoria(Categoria categoria) {
        try {
            categoriaService.eliminar(categoria.getId());
            cargarTodo();
            UiUtils.exito("Categoria eliminada.");
        } catch (MissLuminaException ex) {
            UiUtils.error(ex.getMessage());
        }
    }

    private void eliminarMarca(Marca marca) {
        try {
            marcaService.eliminar(marca.getId());
            cargarTodo();
            UiUtils.exito("Marca eliminada.");
        } catch (MissLuminaException ex) {
            UiUtils.error(ex.getMessage());
        }
    }

    private void eliminarTono(Tono tono) {
        try {
            tonoService.eliminar(tono.getId());
            cargarTodo();
            UiUtils.exito("Tono eliminado.");
        } catch (MissLuminaException ex) {
            UiUtils.error(ex.getMessage());
        }
    }

    private void cargarTodo() {
        try {
            gridCategorias.setItems(categoriaService.listar());
            gridMarcas.setItems(marcaService.listar());
            gridTonos.setItems(tonoService.listar());
        } catch (Exception ex) {
            UiUtils.error("No fue posible cargar las tablas maestras.");
        }
    }
}
