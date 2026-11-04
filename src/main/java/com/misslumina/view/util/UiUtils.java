package com.misslumina.view.util;

import com.misslumina.exception.MissLuminaException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Utilidades de interfaz: notificaciones, dialogos de confirmacion y
 * actualizacion del contador del carrito.
 */
public final class UiUtils {

    private UiUtils() {
    }

    public static void exito(String mensaje) {
        Notification notification = Notification.show(mensaje, 3500,
                Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public static void error(String mensaje) {
        Notification notification = Notification.show(mensaje, 6000,
                Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public static void advertencia(String mensaje) {
        Notification notification = Notification.show(mensaje, 4500,
                Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    public static void info(String mensaje) {
        Notification.show(mensaje, 3500, Notification.Position.TOP_CENTER);
    }

    public static void error(Exception e) {
        if (e instanceof MissLuminaException) {
            error(e.getMessage());
        } else {
            error("Ocurrio un error inesperado. Intente nuevamente.");
        }
    }

    /**
     * Muestra un dialogo de confirmacion y ejecuta la accion solo si la
     * persona confirma.
     */
    public static void confirmar(String titulo, String mensaje, Runnable accion) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(titulo);
        dialog.add(new Span(mensaje));
        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button confirmar = new Button("Confirmar", e -> {
            dialog.close();
            accion.run();
        });
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(cancelar, confirmar);
        dialog.open();
    }

    public static HorizontalLayout fila(Component... componentes) {
        HorizontalLayout fila = new HorizontalLayout(componentes);
        fila.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        fila.setSpacing(true);
        return fila;
    }

    /** Actualiza el contador del carrito del header sin recargar la pagina. */
    public static void actualizarContadorCarrito(int unidades) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().executeJs(
                    "const e = document.getElementById('cart-count');"
                            + "if (e) { e.textContent = $0; }", unidades);
        }
    }
}
