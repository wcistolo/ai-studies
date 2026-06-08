package com.estudos.ecommerce.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoRequest(
        @NotBlank @Size(max = 255) String clienteNome,
        @NotBlank @Email String clienteEmail,
        @NotNull @Size(min = 1) @Valid List<ItemPedidoRequest> itens
) {}
