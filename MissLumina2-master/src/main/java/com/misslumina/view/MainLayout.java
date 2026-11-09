package com.misslumina.view;

import com.misslumina.service.SesionService;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
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

@Layout
public class MainLayout extends AppLayout {

    private final SesionService sesionService;
    private final Avatar avatar = new Avatar();
    private final ContextMenu menuPerfil = new ContextMenu(avatar);
    private final Span contadorCarrito = new Span("0");
    private final Button sesionBoton = new Button();
    private final VerticalLayout menu = new VerticalLayout();
    private final RouterLink marca = new RouterLink("Miss Lumina", InicioView.class);
    private final Button carritoBoton = new Button(new Icon(VaadinIcon.CART));
    private boolean drawerInicial;

    public MainLayout(SesionService sesionService) {
        this.sesionService = sesionService;

        setPrimarySection(Section.NAVBAR);
        setDrawerOpened(false);

        contadorCarrito.setId("cart-count");
        contadorCarrito.addClassName("cart-count");

        addToNavbar(new DrawerToggle(), crearMarca(), crearEspaciador(), crearAcciones());
        menu.setPadding(true);
        menu.setSpacing(false);
        menu.addClassName("main-menu");
        addToDrawer(menu);
    }

    private Component crearMarca() {
        marca.addClassName("app-brand");
        return marca;
    }

    private Component crearEspaciador() {
        Span spacer = new Span();
        spacer.getStyle().set("flex", "1");
        return spacer;
    }

    private Component crearAcciones() {
        avatar.addClassName("navbar-avatar");
        menuPerfil.setOpenOnClick(true);

        carritoBoton.addClickListener(e -> UI.getCurrent().navigate(CarritoView.class));
        carritoBoton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        carritoBoton.addClassName("cart-button");
        carritoBoton.getElement().appendChild(contadorCarrito.getElement());

        sesionBoton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        sesionBoton.setText("Ingresar");
        sesionBoton.addClickListener(e -> UI.getCurrent().navigate(LoginView.class));

        HorizontalLayout acciones = new HorizontalLayout(carritoBoton, avatar, sesionBoton);
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
        boolean admin = sesionService.esAdministrador();
        carritoBoton.setVisible(!admin);
        marca.setRoute(admin ? AdminPanelView.class : InicioView.class);
        if (admin && !drawerInicial) {
            setDrawerOpened(true);
            drawerInicial = true;
        }
        avatar.setVisible(autenticado);
        sesionBoton.setVisible(!autenticado);
        if (autenticado) {
            avatar.setName(sesionService.getUsuarioActual().getNombre());
            menuPerfil.removeAll();
            menuPerfil.addItem("Hola, " + sesionService.getNombreMostrado()).setEnabled(false);
            menuPerfil.addItem("Mi perfil", e -> UI.getCurrent().navigate(PerfilView.class));
            menuPerfil.addItem("Cerrar sesion", e -> cerrarSesion());
        }
        contadorCarrito.setText(String.valueOf(sesionService.getUnidadesCarrito()));
        reconstruirMenu();
    }

    private void cerrarSesion() {
        sesionService.cerrarSesion();
        UiUtils.info("Sesion cerrada. Hasta pronto.");
        UI.getCurrent().getPage().setLocation("/");
    }

    private void reconstruirMenu() {
        menu.removeAll();
        if (sesionService.esAdministrador()) {
            Span seccion = new Span("ADMINISTRACION");
            seccion.addClassName("menu-section");
            menu.add(seccion);
            menu.add(enlace("Panel", AdminPanelView.class));
            menu.add(enlace("Productos", GestionProductosView.class));
            menu.add(enlace("Inventario", GestionInventarioView.class));
            menu.add(enlace("Pedidos", GestionPedidosView.class));
            menu.add(enlace("Categorias y marcas", GestionCategoriasMarcasView.class));
            menu.add(enlace("Reportes", ReportesView.class));
            menu.add(enlace("Mi perfil", PerfilView.class));
            Span tienda = new Span("TIENDA");
            tienda.addClassName("menu-section");
            menu.add(tienda);
            menu.add(enlace("Ver catalogo", CatalogoView.class));
            return;
        }
        menu.add(enlace("Inicio", InicioView.class));
        menu.add(enlace("Catalogo", CatalogoView.class));
        menu.add(enlace("Carrito", CarritoView.class));

        if (sesionService.estaAutenticado()) {
            menu.add(enlace("Mis pedidos", HistorialPedidosView.class));
            menu.add(enlace("Mi perfil", PerfilView.class));
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