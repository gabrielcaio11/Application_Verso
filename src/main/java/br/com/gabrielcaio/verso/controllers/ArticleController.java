package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.ArticleResponseWithTitleAndStatusAndCategoryName;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import br.com.gabrielcaio.verso.dtos.UpdateArticleRequestDTO;
import br.com.gabrielcaio.verso.services.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verso/article")
@RequiredArgsConstructor
@Tag(name = "Article", description = "Endpoints para gerenciamento de artigos")
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "Registro de novo artigo")
    @PostMapping
    public ResponseEntity<CreateArticleResponseDTO> create(@RequestBody CreateArticleRequestDTO dto) {
        var entity = articleService.create(dto);
        return ResponseEntity.ok(entity);
    }

    @Operation(summary = "Buscar todos os artigos com status de rascunho com paginação")
    @GetMapping("/rascunhos")
    public ResponseEntity<Page<ArticleResponseWithTitleAndStatusAndCategoryName>> findAllRascunhos(
            @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageResponse = articleService.findAllArticlesRascunho(pageable);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(summary = "Buscar todos os artigos com paginação")
    @GetMapping
    public ResponseEntity<Page<ArticleResponseWithTitleAndStatusAndCategoryName>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageResponse = articleService.findAllArticles(pageable);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(summary = "Buscar artigo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseWithTitleAndStatusAndCategoryName> findById(
            @PathVariable Long id
    ) {
        var response = articleService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar artigo")
    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponseWithTitleAndStatusAndCategoryName> update(
            @PathVariable Long id,
            @RequestBody UpdateArticleRequestDTO dto
    ) {
        var response = articleService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deletar artigo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}