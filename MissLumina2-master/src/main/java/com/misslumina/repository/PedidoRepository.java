package com.misslumina.repository;

import com.misslumina.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    boolean existsByIdTransaccion(UUID idTransaccion);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    @Query("select distinct p from Pedido p where p.usuario.id = :usuarioId order by p.fechaHoraPedido desc")
    List<Pedido> buscarPorUsuarioConDetalles(@Param("usuarioId") Long usuarioId);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    @Query("select distinct p from Pedido p order by p.fechaHoraPedido desc")
    List<Pedido> buscarTodosConDetalles();

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> buscarPorIdConDetalles(@Param("id") Long id);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    @Query("""
            select distinct p from Pedido p
            where p.fechaPedido between :desde and :hasta
            order by p.fechaHoraPedido desc
            """)
    List<Pedido> buscarPorRangoConDetalles(@Param("desde") LocalDate desde,
                                            @Param("hasta") LocalDate hasta);

    @Query("select coalesce(sum(p.total), 0) from Pedido p where p.estado <> 'CANCELADO'")
    Double totalVentas();

    @Query("""
            select coalesce(sum(p.total), 0) from Pedido p
            where p.estado <> 'CANCELADO' and p.fechaPedido between :desde and :hasta
            """)
    Double totalVentasEntre(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("select count(p) from Pedido p where p.estado <> 'CANCELADO'")
    long contarVentas();

    @Query("""
            select count(p) from Pedido p
            where p.estado <> 'CANCELADO' and p.fechaPedido between :desde and :hasta
            """)
    long contarVentasEntre(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("select p.estado, count(p), coalesce(sum(p.total), 0) from Pedido p group by p.estado order by p.estado")
    List<Object[]> resumenPorEstado();

    @Query("""
            select p.estado, count(p), coalesce(sum(p.total), 0) from Pedido p
            where p.fechaPedido between :desde and :hasta
            group by p.estado order by p.estado
            """)
    List<Object[]> resumenPorEstadoEntre(@Param("desde") LocalDate desde,
                                          @Param("hasta") LocalDate hasta);

    long countByEstado(String estado);
}
