package com.estudos.ecommerce.pedido.service;

import com.estudos.ecommerce.exception.ResourceNotFoundException;
import com.estudos.ecommerce.pedido.domain.ItemPedido;
import com.estudos.ecommerce.pedido.domain.Pedido;
import com.estudos.ecommerce.pedido.domain.StatusPedido;
import com.estudos.ecommerce.pedido.dto.ItemPedidoRequest;
import com.estudos.ecommerce.pedido.dto.ItemPedidoResponse;
import com.estudos.ecommerce.pedido.dto.PedidoRequest;
import com.estudos.ecommerce.pedido.dto.PedidoResponse;
import com.estudos.ecommerce.pedido.repository.PedidoRepository;
import com.estudos.ecommerce.produto.domain.Produto;
import com.estudos.ecommerce.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public List<PedidoResponse> listar() {
        return pedidoRepository.findByStatusNot(StatusPedido.CANCELADO).stream()
                .map(this::toResponse)
                .toList();
    }

    public PedidoResponse buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Pedido pedido = Pedido.builder()
                .clienteNome(request.clienteNome())
                .clienteEmail(request.clienteEmail())
                .total(BigDecimal.ZERO)
                .build();

        List<ItemPedido> itens = buildItens(request.itens(), pedido);
        pedido.getItens().addAll(itens);
        pedido.setTotal(calcularTotal(itens));

        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest request) {
        Pedido pedido = findOrThrow(id);
        pedido.setClienteNome(request.clienteNome());
        pedido.setClienteEmail(request.clienteEmail());

        pedido.getItens().clear();
        List<ItemPedido> novosItens = buildItens(request.itens(), pedido);
        pedido.getItens().addAll(novosItens);
        pedido.setTotal(calcularTotal(pedido.getItens()));

        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = findOrThrow(id);
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    private List<ItemPedido> buildItens(List<ItemPedidoRequest> requests, Pedido pedido) {
        return requests.stream()
                .map(req -> {
                    Produto produto = produtoRepository.findById(req.produtoId())
                            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + req.produtoId()));
                    return ItemPedido.builder()
                            .pedido(pedido)
                            .produto(produto)
                            .quantidade(req.quantidade())
                            .precoUnitario(produto.getPreco())
                            .build();
                })
                .toList();
    }

    private BigDecimal calcularTotal(List<ItemPedido> itens) {
        return itens.stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Pedido findOrThrow(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }

    private PedidoResponse toResponse(Pedido p) {
        List<ItemPedidoResponse> itens = p.getItens().stream()
                .map(i -> new ItemPedidoResponse(
                        i.getId(),
                        i.getProduto().getId(),
                        i.getProduto().getNome(),
                        i.getQuantidade(),
                        i.getPrecoUnitario(),
                        i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()))
                ))
                .toList();

        return new PedidoResponse(
                p.getId(), p.getClienteNome(), p.getClienteEmail(),
                p.getStatus().name(), p.getTotal(), p.getCriadoEm(), itens
        );
    }
}
