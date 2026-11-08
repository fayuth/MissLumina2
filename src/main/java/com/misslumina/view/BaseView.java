package com.misslumina.view;

import com.misslumina.service.core.SesionService;
import com.misslumina.view.auth.LoginView;
import com.misslumina.view.publicc.InicioView;
import com.misslumina.view.util.UiUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;

public abstract class BaseView extends VerticalLayout {

    protected BaseView() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        addClassName("ml-page");
    }

    protected H1 titulo(String texto) {
        H1 h1 = new H1(texto);
        h1.addClassName("ml-page-title");
        return h1;
    }

    protected Span subtitulo(String texto) {
        Span span = new Span(texto);
        span.addClassName("ml-page-subtitle");
        return span;
    }

    protected Div tarjeta(Component... contenido) {
        Div div = new Div(contenido);
        div.addClassName("ml-card");
        return div;
    }

    protected Div panel(String titulo, Component... contenido) {
        Div div = new Div();
        div.addClassName("ml-card");
        H3 h3 = new H3(titulo);
        h3.addClassName("ml-card-title");
        div.add(h3);
        div.add(contenido);
        return div;
    }

    protected HorizontalLayout encabezado(Component... componentes) {
        HorizontalLayout fila = new HorizontalLayout(componentes);
        fila.setWidthFull();
        fila.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        fila.setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        fila.addClassName("ml-page-header");
        return fila;
    }

    protected boolean protegerSesion(BeforeEnterEvent event, SesionService sesion) {
        if (!sesion.estaAutenticado()) {
            UiUtils.advertencia("Debe iniciar sesion para continuar.");
            event.forwardTo(LoginView.class);
            return false;
        }
        return true;
    }

    protected boolean protegerAdministrador(BeforeEnterEvent event, SesionService sesion) {
        if (!protegerSesion(event, sesion)) {
            return false;
        }
        if (!sesion.esAdministrador()) {
            UiUtils.error("No tiene permisos para acceder al panel administrativo.");
            event.forwardTo(InicioView.class);
            return false;
        }
        return true;
    }

    protected Span etiqueta(String texto, String clase) {
        Span span = new Span(texto);
        span.addClassName(clase);
        return span;
    }
}
