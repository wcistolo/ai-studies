package com.estudos.ecommerce.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String clienteNome,
        String clienteEmail,
        String status,
        BigDecimal total,
        LocalDateTime criadoEm,
        List<ItemPedidoResponse> itens
) {}
