package com.misslumina.view;

import com.misslumina.exception.CredencialesInvalidasException;
import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.Usuario;
import com.misslumina.service.SesionService;
import com.misslumina.service.UsuarioService;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("login")
public class LoginView extends BaseView implements BeforeEnterObserver {

    private final UsuarioService usuarioService;
    private final SesionService sesionService;

    public LoginView(UsuarioService usuarioService, SesionService sesionService) {
        this.usuarioService = usuarioService;
        this.sesionService = sesionService;
        setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER);
        setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        construir();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sesionService.estaAutenticado()) {
            event.forwardTo(InicioView.class);
        }
    }

    private void construir() {
        Div tarjeta = new Div();
        tarjeta.addClassName("auth-card");

        H2 titulo = new H2("Bienvenida de nuevo");
        titulo.addClassName("auth-title");

        Span subtitulo = new Span("Ingresa para continuar comprando tus productos favoritos.");
        subtitulo.addClassName("auth-subtitle");

        TextField correo = new TextField("Correo electronico");
        correo.setPlaceholder("tu@correo.com");
        correo.setWidthFull();
        correo.setRequiredIndicatorVisible(true);

        PasswordField contrasena = new PasswordField("Contrasena");
        contrasena.setPlaceholder("Tu contrasena");
        contrasena.setRevealButtonVisible(true);
        contrasena.setWidthFull();
        contrasena.setRequiredIndicatorVisible(true);

        Button ingresar = new Button("Ingresar", e -> {
            try {
                if (correo.isEmpty() || contrasena.isEmpty()) {
                    UiUtils.error("Debe ingresar el correo y la contrasena.");
                    return;
                }
                Usuario usuario = usuarioService.autenticar(correo.getValue(), contrasena.getValue());
                sesionService.iniciarSesion(usuario);
                UiUtils.exito("Bienvenida, " + usuario.getNombreCorto() + ".");
                UI.getCurrent().navigate("");
            } catch (CredencialesInvalidasException ex) {
                UiUtils.error(ex.getMessage());
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            } catch (Exception ex) {
                UiUtils.error("No fue posible iniciar sesion. Intente nuevamente.");
            }
        });
        ingresar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ingresar.setWidthFull();

        HorizontalLayout registro = new HorizontalLayout(
                new Span("Aun no tienes cuenta?"),
                new RouterLink("Registrate aqui", RegistroView.class));
        registro.setSpacing(true);
        registro.addClassName("auth-register");

        FormLayout formulario = new FormLayout(correo, contrasena);
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Div demo = new Div();
        demo.addClassName("demo-box");
        demo.add(new Span("Cuentas de prueba:"),
                new Span("Admin: admin@misslumina.com / Admin123*"),
                new Span("Cliente: cliente@misslumina.com / Cliente123*"));

        tarjeta.add(titulo, subtitulo, formulario, ingresar, registro, demo);
        add(tarjeta);
    }
}
