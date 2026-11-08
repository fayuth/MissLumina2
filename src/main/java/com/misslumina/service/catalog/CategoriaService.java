package com.misslumina.service.catalog;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.model.catalog.Categoria;
import com.misslumina.repository.CategoriaRepository;
import com.misslumina.repository.ProductoRepository;
import com.misslumina.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivaTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<String> listarNombresActivas() {
        return listarActivas().stream().map(Categoria::getNombre).toList();
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        if (categoria == null) {
            throw new DatosInvalidosException("La categoria no puede ser nula.");
        }
        String nombre = ValidacionUtil.limpiar(categoria.getNombre());
        if (ValidacionUtil.textoVacio(nombre)) {
            throw new DatosInvalidosException("El nombre de la categoria es obligatorio.");
        }
        categoriaRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (categoria.getId() == null || !existente.getId().equals(categoria.getId())) {
                throw new DatosInvalidosException("Ya existe la categoria " + nombre + ".");
            }
        });
        categoria.setNombre(nombre);
        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoria guardada: {}", guardada.getNombre());
        return guardada;
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new DatosInvalidosException("La categoria no existe."));
        if (productoRepository.existsByCategoriaIgnoreCase(categoria.getNombre())) {
            throw new DatosInvalidosException(
                    "No se puede eliminar la categoria porque tiene productos asociados.");
        }
        categoriaRepository.delete(categoria);
        categoriaRepository.flush();
        log.info("Categoria eliminada: {}", categoria.getNombre());
    }

    @Transactional(readOnly = true)
    public long contar() {
        return categoriaRepository.count();
    }
}
