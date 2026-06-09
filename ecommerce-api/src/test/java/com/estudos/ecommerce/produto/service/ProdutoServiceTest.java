package com.estudos.ecommerce.produto.service;

import com.estudos.ecommerce.exception.ResourceNotFoundException;
import com.estudos.ecommerce.produto.domain.Produto;
import com.estudos.ecommerce.produto.dto.ProdutoRequest;
import com.estudos.ecommerce.produto.dto.ProdutoResponse;
import com.estudos.ecommerce.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    private Produto produto;
    private ProdutoRequest request;

    @BeforeEach
    void setUp() {
        produto = Produto.builder()
                .id(1L)
                .nome("Notebook")
                .descricao("Notebook gamer")
                .preco(new BigDecimal("4999.99"))
                .quantidade(10)
                .categoria("Eletrônicos")
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        request = new ProdutoRequest("Notebook", "Notebook gamer", new BigDecimal("4999.99"), 10, "Eletrônicos", null);
    }

    @Nested
    @DisplayName("listarAtivos()")
    class ListarAtivos {

        @Test
        @DisplayName("deve retornar lista com produtos ativos")
        void deveRetornarListaDeProdutosAtivos() {
            when(repository.findByAtivoTrue()).thenReturn(List.of(produto));

            List<ProdutoResponse> result = service.listarAtivos();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).nome()).isEqualTo("Notebook");
            assertThat(result.get(0).ativo()).isTrue();
            verify(repository).findByAtivoTrue();
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não houver ativos")
        void deveRetornarListaVazia() {
            when(repository.findByAtivoTrue()).thenReturn(List.of());

            List<ProdutoResponse> result = service.listarAtivos();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar produto quando encontrado")
        void deveRetornarProduto() {
            when(repository.findById(1L)).thenReturn(Optional.of(produto));

            ProdutoResponse result = service.buscarPorId(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.nome()).isEqualTo("Notebook");
            assertThat(result.preco()).isEqualByComparingTo("4999.99");
            assertThat(result.categoria()).isEqualTo("Eletrônicos");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("deve salvar e retornar o produto criado")
        void deveSalvarERetornarProduto() {
            when(repository.save(any(Produto.class))).thenReturn(produto);

            ProdutoResponse result = service.criar(request);

            assertThat(result.nome()).isEqualTo("Notebook");
            assertThat(result.preco()).isEqualByComparingTo("4999.99");
            verify(repository).save(any(Produto.class));
        }

        @Test
        @DisplayName("deve mapear todos os campos do request corretamente")
        void deveMappearCamposCorretamente() {
            ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
            when(repository.save(captor.capture())).thenReturn(produto);

            service.criar(request);

            Produto salvo = captor.getValue();
            assertThat(salvo.getNome()).isEqualTo(request.nome());
            assertThat(salvo.getDescricao()).isEqualTo(request.descricao());
            assertThat(salvo.getPreco()).isEqualByComparingTo(request.preco());
            assertThat(salvo.getQuantidade()).isEqualTo(request.quantidade());
            assertThat(salvo.getCategoria()).isEqualTo(request.categoria());
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar e retornar o produto")
        void deveAtualizarERetornarProduto() {
            ProdutoRequest updateRequest = new ProdutoRequest("Notebook Pro", "Atualizado", new BigDecimal("5999.99"), 5, "Tech", true);
            when(repository.findById(1L)).thenReturn(Optional.of(produto));
            when(repository.save(produto)).thenReturn(produto);

            service.atualizar(1L, updateRequest);

            assertThat(produto.getNome()).isEqualTo("Notebook Pro");
            assertThat(produto.getPreco()).isEqualByComparingTo("5999.99");
            assertThat(produto.getAtivo()).isTrue();
            verify(repository).save(produto);
        }

        @Test
        @DisplayName("não deve alterar ativo quando request.ativo() for null")
        void naoDeveAlterarAtivoQuandoNull() {
            when(repository.findById(1L)).thenReturn(Optional.of(produto));
            when(repository.save(produto)).thenReturn(produto);

            service.atualizar(1L, request); // request.ativo() == null

            assertThat(produto.getAtivo()).isTrue(); // mantém valor original
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("desativar()")
    class Desativar {

        @Test
        @DisplayName("deve setar ativo=false e salvar")
        void deveSetarAtivoFalseESalvar() {
            when(repository.findById(1L)).thenReturn(Optional.of(produto));

            service.desativar(1L);

            assertThat(produto.getAtivo()).isFalse();
            verify(repository).save(produto);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.desativar(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(repository, never()).save(any());
        }
    }
}
