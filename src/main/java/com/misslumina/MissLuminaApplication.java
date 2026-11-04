package com.misslumina;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet("styles.css")
public class MissLuminaApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(MissLuminaApplication.class, args);
    }
}
