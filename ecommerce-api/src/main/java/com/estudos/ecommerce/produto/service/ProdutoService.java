package com.estudos.ecommerce.produto.service;

import com.estudos.ecommerce.exception.ResourceNotFoundException;
import com.estudos.ecommerce.produto.domain.Produto;
import com.estudos.ecommerce.produto.dto.ProdutoRequest;
import com.estudos.ecommerce.produto.dto.ProdutoResponse;
import com.estudos.ecommerce.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;

    public List<ProdutoResponse> listarAtivos() {
        return repository.findByAtivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProdutoResponse buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = Produto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .preco(request.preco())
                .quantidade(request.quantidade())
                .categoria(request.categoria())
                .build();
        return toResponse(repository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = findOrThrow(id);
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setQuantidade(request.quantidade());
        produto.setCategoria(request.categoria());
        if (request.ativo() != null) {
            produto.setAtivo(request.ativo());
        }
        return toResponse(repository.save(produto));
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = findOrThrow(id);
        produto.setAtivo(false);
        repository.save(produto);
    }

    private Produto findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    private ProdutoResponse toResponse(Produto p) {
        return new ProdutoResponse(
                p.getId(), p.getNome(), p.getDescricao(), p.getPreco(),
                p.getQuantidade(), p.getCategoria(), p.getAtivo(), p.getCriadoEm()
        );
    }
}
