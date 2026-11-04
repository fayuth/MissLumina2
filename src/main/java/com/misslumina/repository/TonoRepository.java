package com.misslumina.repository;

import com.misslumina.model.Tono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TonoRepository extends JpaRepository<Tono, Long> {

    List<Tono> findByOrderByNombreAsc();

    List<Tono> findByActivoTrueOrderByNombreAsc();

    Optional<Tono> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
