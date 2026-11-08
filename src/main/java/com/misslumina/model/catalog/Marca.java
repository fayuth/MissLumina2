package com.misslumina.model.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "marcas",
        uniqueConstraints = @UniqueConstraint(name = "uk_marcas_nombre", columnNames = "nombre"))
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(name = "pais_origen", length = 80)
    private String paisOrigen;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    public Marca() {
        this.activa = true;
    }

    public Marca(String nombre, String paisOrigen) {
        this();
        this.nombre = nombre;
        this.paisOrigen = paisOrigen;
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

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return "Marca{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", paisOrigen='" + paisOrigen + '\'' +
                ", activa=" + activa +
                '}';
    }
}
