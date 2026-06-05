package com.estudos.ecommerce.produto.repository;

import com.estudos.ecommerce.produto.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrue();
}
