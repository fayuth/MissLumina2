package com.misslumina.repository;

import com.misslumina.model.catalog.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
            select p from Producto p
            where (:texto = '' or lower(p.nombre) like lower(concat('%', :texto, '%'))
                   or lower(p.marca) like lower(concat('%', :texto, '%'))
                   or lower(p.categoria) like lower(concat('%', :texto, '%'))
                   or lower(coalesce(p.tono, '')) like lower(concat('%', :texto, '%')))
              and (:categoria = '' or lower(p.categoria) = lower(:categoria))
              and (:marca = '' or lower(p.marca) = lower(:marca))
              and (:tono = '' or lower(coalesce(p.tono, '')) = lower(:tono))
            order by p.nombre asc
            """)
    List<Producto> buscar(@Param("texto") String texto,
                          @Param("categoria") String categoria,
                          @Param("marca") String marca,
                          @Param("tono") String tono);

    List<Producto> findByDisponibleTrueOrderByNombreAsc();

    List<Producto> findByStockLessThanEqualOrderByStockAsc(int stock);

    List<Producto> findTop8ByDisponibleTrueOrderByIdDesc();

    Optional<Producto> findByNombreIgnoreCase(String nombre);

    boolean existsByCategoriaIgnoreCase(String categoria);

    boolean existsByMarcaIgnoreCase(String marca);

    boolean existsByTonoIgnoreCase(String tono);

    @Query("select distinct p.tono from Producto p where p.tono is not null and p.tono <> '' order by p.tono")
    List<String> listarTonosUsados();

    @Query("select coalesce(sum(p.stock), 0) from Producto p")
    Long totalUnidadesInventario();

    @Query("select coalesce(sum(p.stock * p.precio), 0) from Producto p")
    Double valorInventario();

    @Query("select count(p) from Producto p where p.stock <= :minimo")
    long contarStockBajo(@Param("minimo") int minimo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarPorIdParaActualizar(@Param("id") Long id);

    @Modifying
    @Query("update Producto p set p.tono = :nuevo where p.tono = :anterior")
    int renombrarTono(@Param("anterior") String anterior, @Param("nuevo") String nuevo);
}
