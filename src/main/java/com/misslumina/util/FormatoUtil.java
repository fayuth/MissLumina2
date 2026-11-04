package com.misslumina.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilidades de presentacion y redondeo monetario.
 * Centralizar el formato evita repetir codigo en las vistas Vaadin.
 */
public final class FormatoUtil {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA_LARGA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", LOCALE_CO);

    private FormatoUtil() {
        // Clase de utilidades: no se instancia.
    }

    /** Formatea un valor como pesos colombianos: $ 32.900. */
    public static String moneda(double valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(LOCALE_CO);
        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);
        return formato.format(valor);
    }

    /** Formatea una fecha LocalDate como dd/MM/yyyy. */
    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    /** Formatea una fecha y hora LocalDateTime como dd/MM/yyyy HH:mm. */
    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora == null ? "" : fechaHora.format(FORMATO_FECHA_HORA);
    }

    /** Formatea una fecha en texto largo (para la portada y el historial). */
    public static String fechaLarga(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA_LARGA);
    }

    /**
     * Redondea a dos decimales con HALF_UP.
     * Se usa en subtotales y totales para que el valor mostrado coincida
     * con el valor NUMERIC(12,2) almacenado en PostgreSQL.
     */
    public static double redondear(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** Calcula un porcentaje de descuento y lo devuelve redondeado. */
    public static double porcentaje(double parte, double total) {
        if (total <= 0) {
            return 0;
        }
        return redondear((parte / total) * 100);
    }
}
