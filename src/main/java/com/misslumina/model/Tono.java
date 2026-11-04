package com.misslumina.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tonos",
        uniqueConstraints = @UniqueConstraint(name = "uk_tonos_nombre", columnNames = "nombre"))
public class Tono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(name = "codigo_hex", length = 7)
    private String codigoHex;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Tono() {
        this.activo = true;
        this.codigoHex = "#CCCCCC";
    }

    public Tono(String nombre, String codigoHex) {
        this();
        this.nombre = nombre;
        this.codigoHex = codigoHex;
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

    public String getCodigoHex() {
        return codigoHex;
    }

    public void setCodigoHex(String codigoHex) {
        this.codigoHex = codigoHex;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Tono{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", codigoHex='" + codigoHex + '\'' +
                ", activo=" + activo +
                '}';
    }
}
