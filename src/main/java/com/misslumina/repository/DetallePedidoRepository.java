package com.misslumina.repository;

import com.misslumina.model.ecommerce.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    boolean existsByProductoId(Long productoId);

    @Query("""
            select coalesce(sum(d.cantidad), 0) from DetallePedido d
            join d.pedido p where p.estado <> 'CANCELADO'
            """)
    Long unidadesVendidas();

    @Query("""
            select coalesce(sum(d.cantidad), 0) from DetallePedido d
            join d.pedido p
            where p.estado <> 'CANCELADO' and p.fechaPedido between :desde and :hasta
            """)
    Long unidadesVendidasEntre(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("""
            select d.producto.nombre, sum(d.cantidad), sum(d.subtotal)
            from DetallePedido d join d.pedido p
            where p.estado <> 'CANCELADO'
            group by d.producto.id, d.producto.nombre
            order by sum(d.cantidad) desc
            """)
    List<Object[]> productosMasVendidos();

    @Query("""
            select d.producto.nombre, sum(d.cantidad), sum(d.subtotal)
            from DetallePedido d join d.pedido p
            where p.estado <> 'CANCELADO' and p.fechaPedido between :desde and :hasta
            group by d.producto.id, d.producto.nombre
            order by sum(d.cantidad) desc
            """)
    List<Object[]> productosMasVendidosEntre(@Param("desde") LocalDate desde,
                                              @Param("hasta") LocalDate hasta);

    @Query("""
            select d.producto.categoria, sum(d.cantidad), sum(d.subtotal)
            from DetallePedido d join d.pedido p
            where p.estado <> 'CANCELADO'
            group by d.producto.categoria
            order by sum(d.subtotal) desc
            """)
    List<Object[]> ventasPorCategoria();

    @Query("""
            select d.producto.categoria, sum(d.cantidad), sum(d.subtotal)
            from DetallePedido d join d.pedido p
            where p.estado <> 'CANCELADO' and p.fechaPedido between :desde and :hasta
            group by d.producto.categoria
            order by sum(d.subtotal) desc
            """)
    List<Object[]> ventasPorCategoriaEntre(@Param("desde") LocalDate desde,
                                           @Param("hasta") LocalDate hasta);
}
