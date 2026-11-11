package com.misslumina.view.cliente;

import com.misslumina.exception.MissLuminaException;
import com.misslumina.model.core.Usuario;
import com.misslumina.service.core.SesionService;
import com.misslumina.service.core.UsuarioService;
import com.misslumina.view.BaseView;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("perfil")
public class PerfilView extends BaseView implements BeforeEnterObserver {

    private final SesionService sesionService;
    private final UsuarioService usuarioService;

    public PerfilView(SesionService sesionService, UsuarioService usuarioService) {
        this.sesionService = sesionService;
        this.usuarioService = usuarioService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!protegerSesion(event, sesionService)) {
            return;
        }
        removeAll();
        construir();
    }

    private void construir() {
        Usuario usuario = sesionService.getUsuarioActual();

        add(titulo("Mi perfil"), subtitulo("Tus datos y la configuracion de tu cuenta."));

        add(crearCabecera(usuario));
        add(panel("Datos personales",
                fila("Nombre", usuario.getNombre()),
                fila("Correo", usuario.getCorreo()),
                fila("Telefono", usuario.getTelefono() == null || usuario.getTelefono().isBlank()
                        ? "Sin registrar" : usuario.getTelefono()),
                fila("Miembro desde", usuario.getFechaRegistroFormateada())));
        add(crearTelefono(usuario));
        add(crearContrasena(usuario));

        Button cerrar = new Button("Cerrar sesion", e -> {
            sesionService.cerrarSesion();
            UiUtils.info("Sesion cerrada. Hasta pronto.");
            UI.getCurrent().getPage().setLocation("/");
        });
        cerrar.addThemeVariants(ButtonVariant.LUMO_ERROR);
        add(cerrar);
    }

    private Div crearCabecera(Usuario usuario) {
        Avatar avatar = new Avatar(usuario.getNombre());
        avatar.getStyle().set("--vaadin-avatar-size", "4.5rem");
        avatar.addClassName("profile-avatar");

        Span nombre = new Span(usuario.getNombre());
        nombre.addClassName("profile-name");
        Span rol = new Span(usuario.esAdministrador() ? "Administradora" : "Cliente");
        rol.addClassName("profile-role");
        Div textos = new Div(nombre, rol);
        textos.addClassName("stat-texts");

        Div cabecera = new Div(avatar, textos);
        cabecera.addClassName("profile-header");
        return cabecera;
    }

    private Div crearTelefono(Usuario usuario) {
        TextField telefono = new TextField("Telefono");
        telefono.setValue(usuario.getTelefono() == null ? "" : usuario.getTelefono());
        telefono.setWidthFull();
        Button guardar = new Button("Guardar telefono", e -> {
            try {
                usuarioService.actualizarTelefono(usuario.getId(), telefono.getValue());
                usuario.setTelefono(telefono.getValue());
                UiUtils.exito("Telefono actualizado.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return panel("Actualizar telefono", telefono, guardar);
    }

    private Div crearContrasena(Usuario usuario) {
        PasswordField actual = new PasswordField("Contrasena actual");
        PasswordField nueva = new PasswordField("Contrasena nueva");
        PasswordField confirmar = new PasswordField("Confirmar contrasena nueva");
        actual.setWidthFull();
        nueva.setWidthFull();
        confirmar.setWidthFull();
        Button cambiar = new Button("Cambiar contrasena", e -> {
            try {
                if (!nueva.getValue().equals(confirmar.getValue())) {
                    UiUtils.error("La confirmacion no coincide con la contrasena nueva.");
                    return;
                }
                usuarioService.cambiarContrasena(usuario.getId(), actual.getValue(),
                        nueva.getValue());
                actual.clear();
                nueva.clear();
                confirmar.clear();
                UiUtils.exito("Contrasena actualizada.");
            } catch (MissLuminaException ex) {
                UiUtils.error(ex.getMessage());
            }
        });
        cambiar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return panel("Cambiar contrasena", actual, nueva, confirmar, cambiar);
    }

    private Div fila(String etiqueta, String valor) {
        Span valorSpan = new Span(valor);
        valorSpan.addClassName("summary-value");
        Div fila = new Div(new Span(etiqueta), valorSpan);
        fila.addClassName("summary-row");
        return fila;
    }
}