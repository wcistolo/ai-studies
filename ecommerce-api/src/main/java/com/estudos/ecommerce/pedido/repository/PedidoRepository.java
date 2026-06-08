package com.estudos.ecommerce.pedido.repository;

import com.estudos.ecommerce.pedido.domain.Pedido;
import com.estudos.ecommerce.pedido.domain.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByStatusNot(StatusPedido status);
}
