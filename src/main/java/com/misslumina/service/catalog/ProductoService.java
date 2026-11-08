package com.misslumina.service.catalog;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.exception.ProductoNoEncontradoException;
import com.misslumina.model.catalog.Producto;
import com.misslumina.repository.CategoriaRepository;
import com.misslumina.repository.DetallePedidoRepository;
import com.misslumina.repository.MarcaRepository;
import com.misslumina.repository.ProductoRepository;
import com.misslumina.repository.TonoRepository;
import com.misslumina.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final TonoRepository tonoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           MarcaRepository marcaRepository,
                           TonoRepository tonoRepository,
                           DetallePedidoRepository detallePedidoRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.tonoRepository = tonoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"));
    }

    @Transactional(readOnly = true)
    public List<Producto> buscar(String texto, String categoria, String marca, String tono) {
        return productoRepository.buscar(
                valorSeguro(texto),
                valorSeguro(categoria),
                valorSeguro(marca),
                valorSeguro(tono));
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "No existe un producto con el id " + id + "."));
    }

    @Transactional(readOnly = true)
    public List<Producto> productosDestacados() {
        return productoRepository.findTop8ByDisponibleTrueOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Producto> productosStockBajo() {
        return productoRepository.findByStockLessThanEqualOrderByStockAsc(Producto.STOCK_MINIMO);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarDisponibles() {
        return productoRepository.findByDisponibleTrueOrderByNombreAsc();
    }

    @Transactional
    public Producto guardar(Producto producto) {
        if (producto == null) {
            throw new DatosInvalidosException("El producto no puede ser nulo.");
        }
        String nombre = ValidacionUtil.limpiar(producto.getNombre());
        if (ValidacionUtil.textoVacio(nombre)) {
            throw new DatosInvalidosException("El nombre del producto es obligatorio.");
        }
        if (producto.getPrecio() <= 0) {
            throw new DatosInvalidosException("El precio debe ser mayor que cero.");
        }
        if (producto.getStock() < 0) {
            throw new DatosInvalidosException("El stock no puede ser negativo.");
        }
        if (producto.getPrecioAnterior() > 0
                && producto.getPrecioAnterior() <= producto.getPrecio()) {
            throw new DatosInvalidosException(
                    "El precio anterior debe ser mayor que el precio actual (o cero si no hay promocion).");
        }
        if (ValidacionUtil.textoVacio(producto.getMarca())) {
            throw new DatosInvalidosException("La marca es obligatoria.");
        }
        if (ValidacionUtil.textoVacio(producto.getCategoria())) {
            throw new DatosInvalidosException("La categoria es obligatoria.");
        }
        if (!marcaRepository.existsByNombreIgnoreCase(producto.getMarca().trim())) {
            throw new DatosInvalidosException("La marca " + producto.getMarca() + " no esta registrada.");
        }
        if (!categoriaRepository.existsByNombreIgnoreCase(producto.getCategoria().trim())) {
            throw new DatosInvalidosException(
                    "La categoria " + producto.getCategoria() + " no esta registrada.");
        }
        if (!ValidacionUtil.textoVacio(producto.getTono())
                && !tonoRepository.existsByNombreIgnoreCase(producto.getTono().trim())) {
            throw new DatosInvalidosException("El tono " + producto.getTono() + " no esta registrado.");
        }

        producto.setNombre(nombre);
        producto.setDescripcion(ValidacionUtil.limpiar(producto.getDescripcion()));
        String imagenUrl = ValidacionUtil.limpiar(producto.getImagenUrl());
        if (imagenUrl != null && imagenUrl.length() > 255) {
            throw new DatosInvalidosException("La ruta de la imagen no puede superar 255 caracteres.");
        }
        producto.setImagenUrl(ValidacionUtil.textoVacio(imagenUrl)
                ? Producto.IMAGEN_POR_DEFECTO : imagenUrl);
        producto.setMarca(producto.getMarca().trim());
        producto.setCategoria(producto.getCategoria().trim());
        producto.setTono(ValidacionUtil.textoVacio(producto.getTono())
                ? null : producto.getTono().trim());
        producto.setDisponible(producto.isDisponible() && producto.getStock() > 0);

        try {
            Producto guardado = productoRepository.save(producto);
            log.info("Producto guardado: {} (id={})", guardado.getNombre(), guardado.getId());
            return guardado;
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al guardar producto {}", nombre, e);
            throw new DatosInvalidosException(
                    "No se pudo guardar el producto. Verifique marca, categoria y tono.");
        }
    }

    @Transactional
    public Producto actualizarStock(Long productoId, int nuevoStock) {
        if (nuevoStock < 0) {
            throw new DatosInvalidosException("El stock no puede ser negativo.");
        }
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("El producto no existe."));
        producto.setStock(nuevoStock);
        producto.setDisponible(nuevoStock > 0);
        Producto guardado = productoRepository.save(producto);
        log.info("Stock actualizado: {} -> {} unidades", producto.getNombre(), nuevoStock);
        return guardado;
    }

    @Transactional
    public void cambiarDisponibilidad(Long productoId, boolean disponible) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("El producto no existe."));
        if (disponible && producto.getStock() <= 0) {
            throw new DatosInvalidosException(
                    "No se puede activar un producto sin stock disponible.");
        }
        producto.setDisponible(disponible);
        productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("El producto no existe."));
        if (detallePedidoRepository.existsByProductoId(productoId)) {
            throw new DatosInvalidosException(
                    "No se puede eliminar el producto porque aparece en pedidos. "
                            + "Puede marcarlo como no disponible.");
        }
        try {
            productoRepository.delete(producto);
            productoRepository.flush();
            log.info("Producto eliminado: {}", producto.getNombre());
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al eliminar producto {}", productoId, e);
            throw new DatosInvalidosException(
                    "No se puede eliminar el producto porque tiene registros relacionados.");
        }
    }

    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        return categoriaRepository.findByActivaTrueOrderByNombreAsc()
                .stream().map(c -> c.getNombre()).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listarMarcas() {
        return marcaRepository.findByActivaTrueOrderByNombreAsc()
                .stream().map(m -> m.getNombre()).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listarTonos() {
        return tonoRepository.findByActivoTrueOrderByNombreAsc()
                .stream().map(t -> t.getNombre()).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listarTonosUsados() {
        return productoRepository.listarTonosUsados();
    }

    @Transactional(readOnly = true)
    public long totalProductos() {
        return productoRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarStockBajo() {
        return productoRepository.contarStockBajo(Producto.STOCK_MINIMO);
    }

    @Transactional(readOnly = true)
    public long unidadesInventario() {
        Long total = productoRepository.totalUnidadesInventario();
        return total == null ? 0 : total;
    }

    @Transactional(readOnly = true)
    public double valorInventario() {
        Double total = productoRepository.valorInventario();
        return total == null ? 0.0 : total;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
