package com.misslumina.view.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/** Tarjeta de indicador usada en el panel administrativo y reportes. */
public class StatCard extends Div {

    public StatCard(String titulo, String valor, String detalle, VaadinIcon icono) {
        addClassName("stat-card");

        Div iconContainer = new Div(new Icon(icono));
        iconContainer.addClassName("stat-icon");

        Span tituloSpan = new Span(titulo);
        tituloSpan.addClassName("stat-title");

        Span valorSpan = new Span(valor);
        valorSpan.addClassName("stat-value");

        Span detalleSpan = new Span(detalle);
        detalleSpan.addClassName("stat-detail");

        Div textos = new Div(tituloSpan, valorSpan, detalleSpan);
        textos.addClassName("stat-texts");

        add(iconContainer, textos);
    }
}
