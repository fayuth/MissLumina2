package com.misslumina.model.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_correo", columnNames = "correo"))
public class Usuario {

    public static final String ROL_CLIENTE = "CLIENTE";
    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "contrasena", nullable = false, length = 128)
    private String contrasena;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    public Usuario() {
        this.rol = ROL_CLIENTE;
        this.fechaRegistro = LocalDate.now();
    }

    public Usuario(String nombre, String correo, String contrasena, String telefono) {
        this();
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean esAdministrador() {
        return ROL_ADMINISTRADOR.equalsIgnoreCase(rol);
    }

    public boolean esCliente() {
        return ROL_CLIENTE.equalsIgnoreCase(rol);
    }

    public void marcarComoAdministrador() {
        this.rol = ROL_ADMINISTRADOR;
    }

    public String getFechaRegistroFormateada() {
        return fechaRegistro == null ? "" : fechaRegistro.format(FORMATO_FECHA);
    }

    public String getNombreCorto() {
        if (nombre == null || nombre.isBlank()) {
            return "Usuario";
        }
        String[] partes = nombre.trim().split("\\s+");
        return partes.length == 1 ? partes[0] : partes[0] + " " + partes[1];
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", rol='" + rol + '\'' +
                ", telefono='" + telefono + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}
