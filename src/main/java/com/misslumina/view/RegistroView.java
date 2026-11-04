package com.misslumina.view;

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

@Route("registro")
public class RegistroView extends BaseView implements BeforeEnterObserver {

    private final UsuarioService usuarioService;
    private final SesionService sesionService;

    public RegistroView(UsuarioService usuarioService, SesionService sesionService) {
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

        H2 titulo = new H2("Crea tu cuenta");
        titulo.addClassName("auth-title");

        Span subtitulo = new Span("Registrate para guardar tus pedidos y comprar mas rapido.");
        subtitulo.addClassName("auth-subtitle");

        TextField nombre = new TextField("Nombre completo");
        nombre.setPlaceholder("Ej. Laura Gomez");
        nombre.setWidthFull();
        nombre.setRequiredIndicatorVisible(true);

        TextField correo = new TextField("Correo electronico");
        correo.setPlaceholder("tu@correo.com");
        correo.setWidthFull();
        correo.setRequiredIndicatorVisible(true);

        TextField telefono = new TextField("Telefono");
        telefono.setPlaceholder("3001234567");
        telefono.setWidthFull();
        telefono.setRequiredIndicatorVisible(true);

        PasswordField contrasena = new PasswordField("Contrasena");
        contrasena.setPlaceholder("Minimo 8 caracteres");
        contrasena.setRevealButtonVisible(true);
        contrasena.setWidthFull();
        contrasena.setRequiredIndicatorVisible(true);

        PasswordField confirmar = new PasswordField("Confirmar contrasena");
        confirmar.setPlaceholder("Repite tu contrasena");
        confirmar.setRevealButtonVisible(true);
        confirmar.setWidthFull();
        confirmar.setRequiredIndicatorVisible(true);

        Button crear = new Button("Crear cuenta", e -> {
            try {
                if (!contrasena.getValue().equals(confirmar.getValue())) {
                    UiUtils.error("Las contrasenas no coinciden.");
                    return;
                }
                Usuario usuario = usuarioService.registrarCliente(
                        nombre.getValue(), correo.getValue(), contrasena.getValue(),
                        telefono.getValue());
                sesionService.iniciarSesion(usuario);
                UiUtils.exito("Cuenta creada. Bienvenida a Miss Lumina.");
                UI.getCurrent().navigate("");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            } catch (Exception ex) {
                UiUtils.error("No fue posible crear la cuenta. Intente nuevamente.");
            }
        });
        crear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        crear.setWidthFull();

        HorizontalLayout login = new HorizontalLayout(
                new Span("Ya tienes una cuenta?"),
                new RouterLink("Inicia sesion", LoginView.class));
        login.setSpacing(true);
        login.addClassName("auth-register");

        FormLayout formulario = new FormLayout(nombre, correo, telefono, contrasena, confirmar);
        formulario.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("640px", 2));
        formulario.setColspan(nombre, 2);
        formulario.setColspan(correo, 2);

        tarjeta.add(titulo, subtitulo, formulario, crear, login);
        add(tarjeta);
    }
}
