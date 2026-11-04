package com.misslumina.model;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.exception.StockInsuficienteException;
import com.misslumina.util.FormatoUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Producto de maquillaje disponible en el catalogo.
 *
 * <p>Ademas de los atributos exigidos, incluye metodos de calculo:
 * subtotal, descuento, valor del inventario y validacion de stock.</p>
 */
@Entity
@Table(name = "productos")
public class Producto {

    /** Umbral para considerar que el inventario esta bajo. */
    public static final int STOCK_MINIMO = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio", nullable = false, columnDefinition = "numeric(12,2)")
    private double precio;

    @Column(name = "precio_anterior", columnDefinition = "numeric(12,2)")
    private double precioAnterior;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "marca", nullable = false, length = 80)
    private String marca;

    @Column(name = "categoria", nullable = false, length = 80)
    private String categoria;

    @Column(name = "tono", length = 80)
    private String tono;

    @Column(name = "disponible", nullable = false)
    private boolean disponible;

    /** Constructor por defecto. */
    public Producto() {
        this.disponible = true;
        this.precioAnterior = 0.0;
        this.stock = 0;
    }

    /** Constructor parametrizado para crear productos de prueba o desde el panel. */
    public Producto(String nombre, String descripcion, double precio, double precioAnterior,
                    int stock, String marca, String categoria, String tono) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioAnterior = precioAnterior;
        this.stock = stock;
        this.marca = marca;
        this.categoria = categoria;
        this.tono = tono;
        this.disponible = stock > 0;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getPrecioAnterior() {
        return precioAnterior;
    }

    public void setPrecioAnterior(double precioAnterior) {
        this.precioAnterior = precioAnterior;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTono() {
        return tono;
    }

    public void setTono(String tono) {
        this.tono = tono;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    /**
     * Funcion de calculo: subtotal de una cantidad de este producto.
     * Ejemplo: 3 labiales de $32.900 = $98.700.
     */
    public double calcularSubtotal(int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor que cero.");
        }
        return FormatoUtil.redondear(cantidad * precio);
    }

    /**
     * Funcion de calculo: descuento unitario cuando existe precio anterior.
     * Si no hay promocion, el descuento es cero.
     */
    public double calcularDescuentoUnitario() {
        if (precioAnterior > precio) {
            return FormatoUtil.redondear(precioAnterior - precio);
        }
        return 0.0;
    }

    /** Funcion: porcentaje de descuento respecto al precio anterior. */
    public double calcularPorcentajeDescuento() {
        if (precioAnterior <= 0 || precioAnterior <= precio) {
            return 0.0;
        }
        return FormatoUtil.porcentaje(calcularDescuentoUnitario(), precioAnterior);
    }

    /** Funcion: valor total del inventario de este producto. */
    public double calcularValorInventario() {
        return FormatoUtil.redondear(stock * precio);
    }

    /** Funcion: verifica si hay existencias suficientes. */
    public boolean hayStock(int cantidad) {
        return disponible && cantidad > 0 && stock >= cantidad;
    }

    /** Funcion: verifica si el stock esta por debajo del minimo. */
    public boolean esStockBajo() {
        return stock <= STOCK_MINIMO;
    }

    /**
     * Procedimiento: descuenta unidades del inventario.
     * Lanza una excepcion de negocio si no hay suficientes existencias.
     */
    public void descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad a descontar debe ser mayor que cero.");
        }
        if (stock < cantidad) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para " + nombre + ". Disponible: " + stock
                            + ", solicitado: " + cantidad);
        }
        this.stock -= cantidad;
        if (this.stock == 0) {
            this.disponible = false;
        }
    }

    /** Procedimiento: repone unidades (por ejemplo, al cancelar un pedido). */
    public void reponerStock(int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad a reponer debe ser mayor que cero.");
        }
        this.stock += cantidad;
        this.disponible = true;
    }

    /** Procedimiento: sincroniza la bandera disponible con el stock. */
    public void actualizarDisponibilidad() {
        // Se respeta una desactivacion manual del administrador.
        this.disponible = this.stock > 0 && this.disponible;
    }

    public String getPrecioFormateado() {
        return FormatoUtil.moneda(precio);
    }

    public String getPrecioAnteriorFormateado() {
        return precioAnterior > 0 ? FormatoUtil.moneda(precioAnterior) : "";
    }

    public String getDescuentoFormateado() {
        return FormatoUtil.moneda(calcularDescuentoUnitario());
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", precioAnterior=" + precioAnterior +
                ", stock=" + stock +
                ", marca='" + marca + '\'' +
                ", categoria='" + categoria + '\'' +
                ", tono='" + tono + '\'' +
                ", disponible=" + disponible +
                '}';
    }
}
