package com.misslumina.config;

import com.misslumina.model.core.Usuario;
import com.misslumina.repository.UsuarioRepository;
import com.misslumina.util.PasswordUtil;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final String adminNombre;
    private final String adminCorreo;
    private final String adminClave;
    private final String adminTelefono;
    private final String clienteNombre;
    private final String clienteCorreo;
    private final String clienteClave;
    private final String clienteTelefono;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           @Value("${app.admin.nombre:}") String adminNombre,
                           @Value("${app.admin.correo:}") String adminCorreo,
                           @Value("${app.admin.clave:}") String adminClave,
                           @Value("${app.admin.telefono:}") String adminTelefono,
                           @Value("${app.cliente.nombre:}") String clienteNombre,
                           @Value("${app.cliente.correo:}") String clienteCorreo,
                           @Value("${app.cliente.clave:}") String clienteClave,
                           @Value("${app.cliente.telefono:}") String clienteTelefono) {
        this.usuarioRepository = usuarioRepository;
        this.adminNombre = adminNombre;
        this.adminCorreo = adminCorreo;
        this.adminClave = adminClave;
        this.adminTelefono = adminTelefono;
        this.clienteNombre = clienteNombre;
        this.clienteCorreo = clienteCorreo;
        this.clienteClave = clienteClave;
        this.clienteTelefono = clienteTelefono;
    }

    @Override
    public void run(String @NonNull ... args) {
        try {
            if (adminCorreo.isBlank() && clienteCorreo.isBlank()) {
                log.info("Siembra de usuarios desactivada: defina APP_ADMIN_CORREO/APP_ADMIN_CLAVE "
                        + "o APP_CLIENTE_CORREO/APP_CLIENTE_CLAVE.");
                return;
            }
            sembrar(adminNombre, adminCorreo, adminClave, adminTelefono, Usuario.ROL_ADMINISTRADOR);
            sembrar(clienteNombre, clienteCorreo, clienteClave, clienteTelefono, Usuario.ROL_CLIENTE);
        } catch (Exception e) {
            log.error("No se pudieron cargar los datos iniciales. "
                    + "Verifique la conexion con PostgreSQL.", e);
        }
    }

    private void sembrar(String nombre, String correo, String clave, String telefono, String rol) {
        if (correo.isBlank() || clave.isBlank()) {
            return;
        }
        if (usuarioRepository.findByCorreoIgnoreCase(correo).isPresent()) {
            return;
        }
        Usuario usuario = new Usuario(nombre, correo, PasswordUtil.hashear(clave), telefono);
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
        log.info("Usuario {} creado: {}", rol, correo);
    }
}
