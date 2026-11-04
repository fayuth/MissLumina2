package com.misslumina.service;

import com.misslumina.exception.CredencialesInvalidasException;
import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.exception.ErrorBaseDatosException;
import com.misslumina.model.Usuario;
import com.misslumina.repository.UsuarioRepository;
import com.misslumina.util.PasswordUtil;
import com.misslumina.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de usuarios: registro, autenticacion y consultas.
 * Toda la logica de negocio queda fuera de las vistas Vaadin.
 */
@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra un cliente. Valida datos, verifica que el correo no exista
     * y guarda la contrasena como hash SHA-256.
     */
    @Transactional
    public Usuario registrarCliente(String nombre, String correo, String contrasena,
                                    String telefono) {
        String nombreLimpio = ValidacionUtil.limpiar(nombre);
        String correoLimpio = ValidacionUtil.limpiar(correo);
        String telefonoLimpio = ValidacionUtil.limpiar(telefono);

        if (ValidacionUtil.textoVacio(nombreLimpio)) {
            throw new DatosInvalidosException("El nombre es obligatorio.");
        }
        if (!ValidacionUtil.correoValido(correoLimpio)) {
            throw new DatosInvalidosException("El correo no tiene un formato valido.");
        }
        if (contrasena == null || contrasena.length() < Usuario.LONGITUD_MINIMA_CONTRASENA) {
            throw new DatosInvalidosException(
                    "La contrasena debe tener al menos " + Usuario.LONGITUD_MINIMA_CONTRASENA
                            + " caracteres.");
        }
        if (!ValidacionUtil.telefonoValido(telefonoLimpio)) {
            throw new DatosInvalidosException("El telefono no tiene un formato valido.");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)) {
            throw new DatosInvalidosException("Ya existe una cuenta con el correo " + correoLimpio);
        }

        try {
            Usuario usuario = new Usuario(nombreLimpio, correoLimpio.toLowerCase(),
                    PasswordUtil.hashear(contrasena), telefonoLimpio);
            usuario.setRol(Usuario.ROL_CLIENTE);
            Usuario guardado = usuarioRepository.save(usuario);
            log.info("Cliente registrado: {} ({})", guardado.getCorreo(), guardado.getId());
            return guardado;
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al registrar usuario {}", correoLimpio, e);
            throw new DatosInvalidosException("No se pudo registrar la cuenta: el correo ya existe.");
        } catch (DataAccessException e) {
            log.error("Error de conexion con PostgreSQL al registrar usuario", e);
            throw new ErrorBaseDatosException(
                    "No se pudo conectar con la base de datos. Intente nuevamente.", e);
        }
    }

    /**
     * Autentica por correo y contrasena.
     * El mensaje de error es generico para no revelar si el correo existe.
     */
    @Transactional(readOnly = true)
    public Usuario autenticar(String correo, String contrasena) {
        String correoLimpio = ValidacionUtil.limpiar(correo);
        if (ValidacionUtil.textoVacio(correoLimpio) || ValidacionUtil.textoVacio(contrasena)) {
            throw new CredencialesInvalidasException("Debe ingresar el correo y la contrasena.");
        }
        Optional<Usuario> encontrado = usuarioRepository.findByCorreoIgnoreCase(correoLimpio);
        if (encontrado.isEmpty()
                || !PasswordUtil.verificar(contrasena, encontrado.get().getContrasena())) {
            log.warn("Intento de inicio de sesion fallido para {}", correoLimpio);
            throw new CredencialesInvalidasException("Correo o contrasena incorrectos.");
        }
        log.info("Inicio de sesion exitoso: {}", correoLimpio);
        return encontrado.get();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(ValidacionUtil.limpiar(correo));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new DatosInvalidosException("El usuario no existe."));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarClientes() {
        return usuarioRepository.findByRolOrderByNombreAsc(Usuario.ROL_CLIENTE);
    }

    @Transactional(readOnly = true)
    public long contarClientes() {
        return usuarioRepository.countByRol(Usuario.ROL_CLIENTE);
    }

    @Transactional(readOnly = true)
    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    /** Procedimiento: actualiza el telefono del usuario. */
    @Transactional
    public void actualizarTelefono(Long usuarioId, String telefono) {
        Usuario usuario = buscarPorId(usuarioId);
        if (!ValidacionUtil.telefonoValido(telefono)) {
            throw new DatosInvalidosException("El telefono no tiene un formato valido.");
        }
        usuario.setTelefono(ValidacionUtil.limpiar(telefono));
        usuarioRepository.save(usuario);
    }

    /** Procedimiento: cambia la contrasena guardando siempre el hash. */
    @Transactional
    public void cambiarContrasena(Long usuarioId, String contrasenaActual, String contrasenaNueva) {
        Usuario usuario = buscarPorId(usuarioId);
        if (!PasswordUtil.verificar(contrasenaActual, usuario.getContrasena())) {
            throw new CredencialesInvalidasException("La contrasena actual no es correcta.");
        }
        if (contrasenaNueva == null || contrasenaNueva.length() < Usuario.LONGITUD_MINIMA_CONTRASENA) {
            throw new DatosInvalidosException("La nueva contrasena es demasiado corta.");
        }
        usuario.setContrasena(PasswordUtil.hashear(contrasenaNueva));
        usuarioRepository.save(usuario);
        log.info("Contrasena actualizada para usuario {}", usuarioId);
    }
}
