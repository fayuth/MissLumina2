package com.misslumina.service;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.model.Marca;
import com.misslumina.repository.MarcaRepository;
import com.misslumina.repository.ProductoRepository;
import com.misslumina.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarcaService {

    private static final Logger log = LoggerFactory.getLogger(MarcaService.class);

    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;

    public MarcaService(MarcaRepository marcaRepository,
                        ProductoRepository productoRepository) {
        this.marcaRepository = marcaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Marca> listar() {
        return marcaRepository.findByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Marca> listarActivas() {
        return marcaRepository.findByActivaTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<String> listarNombresActivas() {
        return listarActivas().stream().map(Marca::getNombre).toList();
    }

    @Transactional
    public Marca guardar(Marca marca) {
        if (marca == null) {
            throw new DatosInvalidosException("La marca no puede ser nula.");
        }
        String nombre = ValidacionUtil.limpiar(marca.getNombre());
        if (ValidacionUtil.textoVacio(nombre)) {
            throw new DatosInvalidosException("El nombre de la marca es obligatorio.");
        }
        marcaRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (marca.getId() == null || !existente.getId().equals(marca.getId())) {
                throw new DatosInvalidosException("Ya existe la marca " + nombre + ".");
            }
        });
        marca.setNombre(nombre);
        Marca guardada = marcaRepository.save(marca);
        log.info("Marca guardada: {}", guardada.getNombre());
        return guardada;
    }

    @Transactional
    public void eliminar(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new DatosInvalidosException("La marca no existe."));
        if (productoRepository.existsByMarcaIgnoreCase(marca.getNombre())) {
            throw new DatosInvalidosException(
                    "No se puede eliminar la marca porque tiene productos asociados.");
        }
        marcaRepository.delete(marca);
        marcaRepository.flush();
        log.info("Marca eliminada: {}", marca.getNombre());
    }

    @Transactional(readOnly = true)
    public long contar() {
        return marcaRepository.count();
    }
}
