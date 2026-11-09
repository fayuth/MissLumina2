package com.misslumina.service;

import com.misslumina.exception.DatosInvalidosException;
import com.misslumina.model.Tono;
import com.misslumina.repository.ProductoRepository;
import com.misslumina.repository.TonoRepository;
import com.misslumina.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TonoService {

    private static final Logger log = LoggerFactory.getLogger(TonoService.class);

    private final TonoRepository tonoRepository;
    private final ProductoRepository productoRepository;

    public TonoService(TonoRepository tonoRepository,
                       ProductoRepository productoRepository) {
        this.tonoRepository = tonoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Tono> listar() {
        return tonoRepository.findByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Tono> listarActivos() {
        return tonoRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<String> listarNombresActivos() {
        return listarActivos().stream().map(Tono::getNombre).toList();
    }

    @Transactional
    public Tono guardar(Tono tono) {
        if (tono == null) {
            throw new DatosInvalidosException("El tono no puede ser nulo.");
        }
        String nombre = ValidacionUtil.limpiar(tono.getNombre());
        if (ValidacionUtil.textoVacio(nombre)) {
            throw new DatosInvalidosException("El nombre del tono es obligatorio.");
        }
        tonoRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (tono.getId() == null || !existente.getId().equals(tono.getId())) {
                throw new DatosInvalidosException("Ya existe el tono " + nombre + ".");
            }
        });

        if (tono.getId() != null) {
            Tono anterior = tonoRepository.findById(tono.getId()).orElse(null);
            if (anterior != null && !anterior.getNombre().equalsIgnoreCase(nombre)) {
                productoRepository.renombrarTono(anterior.getNombre(), nombre);
            }
        }
        tono.setNombre(nombre);
        Tono guardado = tonoRepository.save(tono);
        log.info("Tono guardado: {}", guardado.getNombre());
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        Tono tono = tonoRepository.findById(id)
                .orElseThrow(() -> new DatosInvalidosException("El tono no existe."));
        if (productoRepository.existsByTonoIgnoreCase(tono.getNombre())) {
            throw new DatosInvalidosException(
                    "No se puede eliminar el tono porque tiene productos asociados.");
        }
        tonoRepository.delete(tono);
        tonoRepository.flush();
        log.info("Tono eliminado: {}", tono.getNombre());
    }

    @Transactional(readOnly = true)
    public long contar() {
        return tonoRepository.count();
    }
}
