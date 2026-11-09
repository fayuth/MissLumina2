package com.misslumina.repository;

import com.misslumina.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    List<Marca> findByOrderByNombreAsc();

    List<Marca> findByActivaTrueOrderByNombreAsc();

    Optional<Marca> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
