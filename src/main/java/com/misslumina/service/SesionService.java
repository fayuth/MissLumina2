package com.misslumina.service;

import com.misslumina.model.Carrito;
import com.misslumina.model.Usuario;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Maneja el estado de la sesion web: usuario autenticado y carrito.
 *
 * <p>No se usa Spring Security para mantener el proyecto simple y facil de
 * explicar. El usuario se guarda en la {@link VaadinSession}; las vistas
 * administrativas verifican el rol antes de mostrar informacion sensible.</p>
 */
@Service
public class SesionService {

    private static final Logger log = LoggerFactory.getLogger(SesionService.class);

    public static final String ATRIBUTO_USUARIO = "misslumina.usuario";
    public static final String ATRIBUTO_CARRITO = "misslumina.carrito";

    public void iniciarSesion(Usuario usuario) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            throw new IllegalStateException("No hay una sesion Vaadin activa.");
        }
        session.setAttribute(ATRIBUTO_USUARIO, usuario);
        session.setAttribute(ATRIBUTO_CARRITO, new Carrito());
        log.info("Sesion iniciada para {}", usuario.getCorreo());
    }

    public void cerrarSesion() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return;
        }
        Object usuario = session.getAttribute(ATRIBUTO_USUARIO);
        session.setAttribute(ATRIBUTO_USUARIO, null);
        session.setAttribute(ATRIBUTO_CARRITO, null);
        if (usuario instanceof Usuario u) {
            log.info("Sesion cerrada para {}", u.getCorreo());
        }
    }

    public Usuario getUsuarioActual() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return null;
        }
        Object valor = session.getAttribute(ATRIBUTO_USUARIO);
        return valor instanceof Usuario usuario ? usuario : null;
    }

    public boolean estaAutenticado() {
        return getUsuarioActual() != null;
    }

    public boolean esAdministrador() {
        Usuario usuario = getUsuarioActual();
        return usuario != null && usuario.esAdministrador();
    }

    public Long getUsuarioActualId() {
        Usuario usuario = getUsuarioActual();
        return usuario == null ? null : usuario.getId();
    }

    public String getNombreMostrado() {
        Usuario usuario = getUsuarioActual();
        return usuario == null ? "Invitada" : usuario.getNombreCorto();
    }

    /**
     * Devuelve el carrito de la sesion. Si no existe lo crea.
     * Cada cliente que abre la tienda obtiene un carrito independiente.
     */
    public Carrito getCarrito() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return new Carrito();
        }
        Object valor = session.getAttribute(ATRIBUTO_CARRITO);
        if (valor instanceof Carrito carrito) {
            return carrito;
        }
        Carrito carrito = new Carrito();
        session.setAttribute(ATRIBUTO_CARRITO, carrito);
        return carrito;
    }

    public int getUnidadesCarrito() {
        return getCarrito().getCantidadTotal();
    }
}
