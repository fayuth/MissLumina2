package com.misslumina.view.publicc;

import com.misslumina.model.catalog.Categoria;
import com.misslumina.model.catalog.Producto;
import com.misslumina.service.ecommerce.CarritoService;
import com.misslumina.service.catalog.CategoriaService;
import com.misslumina.service.catalog.ProductoService;
import com.misslumina.service.core.SesionService;
import com.misslumina.view.BaseView;
import com.misslumina.view.auth.RegistroView;
import com.misslumina.view.cliente.CatalogoView;
import com.misslumina.view.components.ProductoCard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Route("")
@RouteAlias("inicio")
public class InicioView extends BaseView {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final CarritoService carritoService;
    private final SesionService sesionService;

    public InicioView(ProductoService productoService, CategoriaService categoriaService,
                      CarritoService carritoService, SesionService sesionService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.carritoService = carritoService;
        this.sesionService = sesionService;
        setPadding(false);
        setSpacing(false);
        removeClassName("ml-page");
        addClassName("home-page");
        construir();
    }

    private void construir() {
        add(crearHero());
        add(crearCategorias());
        add(crearDestacados());
        add(crearBeneficios());
    }

    private Div crearHero() {
        Div hero = new Div();
        hero.addClassName("hero");

        Div contenido = new Div();
        contenido.addClassName("hero-content");

        Span eyebrow = new Span("Nueva coleccion");
        eyebrow.addClassName("hero-eyebrow");

        H2 titulo = new H2("Tu maquillaje, tu luz.");
        titulo.addClassName("hero-title");

        Span texto = new Span(
                "Descubre labiales, bases, sombras y mucho mas. Productos originales, "
                        + "tonos para cada ocasion y envios a todo el pais.");
        texto.addClassName("hero-text");

        Button catalogo = new Button("Explorar catalogo",
                e -> UI.getCurrent().navigate(CatalogoView.class));
        catalogo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout acciones = new HorizontalLayout(catalogo);
        if (!sesionService.estaAutenticado()) {
            Button registro = new Button("Crear mi cuenta",
                    e -> UI.getCurrent().navigate(RegistroView.class));
            acciones.add(registro);
        }
        acciones.setSpacing(true);
        acciones.addClassName("hero-actions");

        contenido.add(eyebrow, titulo, texto, acciones);
        hero.add(contenido);
        return hero;
    }

    private Div crearCategorias() {
        Div seccion = new Div();
        seccion.addClassName("home-section");

        H2 titulo = new H2("Compra por categoria");
        titulo.addClassName("section-title");
        seccion.add(titulo);

        HorizontalLayout chips = new HorizontalLayout();
        chips.setWrap(true);
        chips.addClassName("category-chips");
        List<Categoria> categorias = categoriaService.listarActivas();
        for (Categoria categoria : categorias) {
            Button chip = new Button(categoria.getNombre(), e -> {
                String url = "catalogo?categoria="
                        + URLEncoder.encode(categoria.getNombre(), StandardCharsets.UTF_8);
                UI.getCurrent().navigate(url);
            });
            chip.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            chip.addClassName("category-chip");
            chips.add(chip);
        }
        seccion.add(chips);
        return seccion;
    }

    private Div crearDestacados() {
        Div seccion = new Div();
        seccion.addClassName("home-section");

        HorizontalLayout cabecera = new HorizontalLayout();
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        H2 titulo = new H2("Recien llegados");
        titulo.addClassName("section-title");
        Button verTodo = new Button("Ver todo", e -> UI.getCurrent().navigate(CatalogoView.class));
        verTodo.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cabecera.add(titulo, verTodo);
        seccion.add(cabecera);

        List<Producto> productos = productoService.productosDestacados();
        if (productos.isEmpty()) {
            Span vacio = new Span("Todavia no hay productos publicados.");
            vacio.addClassName("empty-state");
            seccion.add(vacio);
            return seccion;
        }

        FlexLayout grid = new FlexLayout();
        grid.addClassName("product-grid");
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        for (Producto producto : productos) {
            grid.add(new ProductoCard(producto, carritoService));
        }
        seccion.add(grid);
        return seccion;
    }

    private Div crearBeneficios() {
        Div seccion = new Div();
        seccion.addClassName("benefits");
        seccion.add(beneficio("Envios rapidos", "Recibe tu pedido en 2 a 5 dias habiles."));
        seccion.add(beneficio("Productos originales", "Trabajamos con marcas reconocidas."));
        seccion.add(beneficio("Promociones", "Descuentos especiales cada semana."));
        return seccion;
    }

    private Div beneficio(String titulo, String texto) {
        Div div = new Div();
        div.addClassName("benefit-card");
        Span tituloSpan = new Span(titulo);
        tituloSpan.addClassName("benefit-title");
        Span textoSpan = new Span(texto);
        textoSpan.addClassName("benefit-text");
        div.add(tituloSpan, textoSpan);
        return div;
    }
}
