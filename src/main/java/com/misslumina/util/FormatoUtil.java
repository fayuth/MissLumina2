package com.misslumina.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatoUtil {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA_LARGA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", LOCALE_CO);

    private FormatoUtil() {
    }

    public static String moneda(double valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(LOCALE_CO);
        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);
        return formato.format(valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora == null ? "" : fechaHora.format(FORMATO_FECHA_HORA);
    }

    public static String fechaLarga(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA_LARGA);
    }

    public static double redondear(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static double porcentaje(double parte, double total) {
        if (total <= 0) {
            return 0;
        }
        return redondear((parte / total) * 100);
    }
}
