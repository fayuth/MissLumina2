package com.misslumina.view;

import com.misslumina.service.SesionService;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;

/**
 * Layout principal de la tienda: barra superior, menu lateral y contenido.
 * Vaadin lo aplica automaticamente a todas las vistas que no indican otro
 * layout, gracias a la anotacion {@link Layout}.
 */
@Layout
public class MainLayout extends AppLayout {

    private final SesionService sesionService;
    private final Span usuarioSpan = new Span();
    private final Span contadorCarrito = new Span("0");
    private final Button sesionBoton = new Button();
    private final VerticalLayout menu = new VerticalLayout();

    public MainLayout(SesionService sesionService) {
        this.sesionService = sesionService;

        setPrimarySection(Section.NAVBAR);
        setDrawerOpened(false);

        contadorCarrito.setId("cart-count");
        contadorCarrito.addClassName("cart-count");

        addToNavbar(crearMarca(), crearEspaciador(), crearAcciones());
        menu.setPadding(true);
        menu.setSpacing(false);
        menu.addClassName("main-menu");
        addToDrawer(menu);
    }

    private Component crearMarca() {
        RouterLink marca = new RouterLink("Miss Lumina", InicioView.class);
        marca.addClassName("app-brand");
        return marca;
    }

    private Component crearEspaciador() {
        Span spacer = new Span();
        spacer.getStyle().set("flex", "1");
        return spacer;
    }

    private Component crearAcciones() {
        usuarioSpan.addClassName("navbar-user");

        Button carrito = new Button(new Icon(VaadinIcon.CART), e ->
                UI.getCurrent().navigate(CarritoView.class));
        carrito.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        carrito.addClassName("cart-button");
        carrito.getElement().appendChild(contadorCarrito.getElement());

        sesionBoton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        sesionBoton.addClickListener(e -> {
            if (sesionService.estaAutenticado()) {
                sesionService.cerrarSesion();
                UiUtils.info("Sesion cerrada. Hasta pronto.");
                UI.getCurrent().getPage().setLocation("/");
            } else {
                UI.getCurrent().navigate(LoginView.class);
            }
        });

        HorizontalLayout acciones = new HorizontalLayout(usuarioSpan, carrito, sesionBoton);
        acciones.setAlignItems(FlexComponent.Alignment.CENTER);
        acciones.setSpacing(true);
        acciones.addClassName("navbar-actions");
        return acciones;
    }

    @Override
    public void showRouterLayoutContent(com.vaadin.flow.component.HasElement content) {
        super.showRouterLayoutContent(content);
        actualizarEstado();
    }

    private void actualizarEstado() {
        boolean autenticado = sesionService.estaAutenticado();
        usuarioSpan.setText(autenticado ? "Hola, " + sesionService.getNombreMostrado() : "");
        sesionBoton.setText(autenticado ? "Salir" : "Ingresar");
        contadorCarrito.setText(String.valueOf(sesionService.getUnidadesCarrito()));
        reconstruirMenu();
    }

    private void reconstruirMenu() {
        menu.removeAll();
        menu.add(enlace("Inicio", InicioView.class));
        menu.add(enlace("Catalogo", CatalogoView.class));
        menu.add(enlace("Carrito", CarritoView.class));

        if (sesionService.estaAutenticado()) {
            menu.add(enlace("Mis pedidos", HistorialPedidosView.class));
        }
        if (sesionService.esAdministrador()) {
            Span separador = new Span("ADMINISTRACION");
            separador.addClassName("menu-section");
            menu.add(separador);
            menu.add(enlace("Panel", AdminPanelView.class));
            menu.add(enlace("Productos", GestionProductosView.class));
            menu.add(enlace("Inventario", GestionInventarioView.class));
            menu.add(enlace("Pedidos", GestionPedidosView.class));
            menu.add(enlace("Categorias y marcas", GestionCategoriasMarcasView.class));
            menu.add(enlace("Reportes", ReportesView.class));
        }
        if (!sesionService.estaAutenticado()) {
            menu.add(enlace("Iniciar sesion", LoginView.class));
            menu.add(enlace("Registrarme", RegistroView.class));
        }
    }

    private RouterLink enlace(String texto, Class<? extends Component> destino) {
        RouterLink link = new RouterLink(texto, destino);
        link.addClassName("menu-link");
        return link;
    }
}
