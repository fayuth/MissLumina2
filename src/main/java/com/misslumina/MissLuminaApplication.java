package com.misslumina;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada unico de Miss Lumina.
 *
 * <p>Esta es la UNICA clase del proyecto que contiene un metodo {@code main}.
 * Spring Boot levanta el contenedor de dependencias, configura JPA/Hibernate,
 * abre el pool de conexiones hacia PostgreSQL y publica las vistas Vaadin.</p>
 *
 * <p>La anotacion {@link SpringBootApplication} activa el escaneo de
 * componentes dentro de {@code com.misslumina}.</p>
 */
@SpringBootApplication
@StyleSheet("styles.css")
public class MissLuminaApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(MissLuminaApplication.class, args);
    }
}
