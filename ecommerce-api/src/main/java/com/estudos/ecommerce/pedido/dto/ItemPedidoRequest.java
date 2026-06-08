package com.estudos.ecommerce.pedido.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequest(
        @NotNull Long produtoId,
        @NotNull @Positive Integer quantidade
) {}
