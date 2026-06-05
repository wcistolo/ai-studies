package com.estudos.ecommerce.produto.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidade,
        String categoria,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
