package com.misslumina.view.components;

import com.vaadin.flow.component.html.Span;

public class EstadoBadge extends Span {

    public EstadoBadge(String estado) {
        super(estado == null ? "" : estado);
        addClassName("estado-badge");
        if (estado != null) {
            addClassName("estado-" + estado.toLowerCase());
        }
    }
}
