package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.ArticleResponseWithTitleAndStatusAndCategoryName;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import br.com.gabrielcaio.verso.dtos.UpdateArticleRequestDTO;
import br.com.gabrielcaio.verso.services.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(
            summary = "Registro de novo artigo",
            description = "Cria um novo artigo. O usuário autenticado será definido como autor automaticamente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Artigo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = CreateArticleResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - autenticação necessária"),
            @ApiResponse(responseCode = "422", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<CreateArticleResponseDTO> create(
            @RequestBody(
                    description = "Dados do artigo a ser criado",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateArticleRequestDTO.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateArticleRequestDTO dto) {
        var entity = articleService.create(dto);
        return ResponseEntity.ok(entity);
    }

    @Operation(
            summary = "Buscar todos os artigos publicados com paginação",
            description = "Retorna uma lista paginada de artigos com status PUBLICADO. Apenas artigos publicados são visíveis."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de artigos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<Page<ArticleResponseWithTitleAndStatusAndCategoryName>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageResponse = articleService.findAllArticles(pageable);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(
            summary = "Buscar artigos em rascunho do usuário autenticado",
            description = "Retorna uma lista paginada de artigos com status RASCUNHO do usuário autenticado. Apenas o autor pode ver seus próprios rascunhos."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de rascunhos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/rascunhos")
    public ResponseEntity<Page<ArticleResponseWithTitleAndStatusAndCategoryName>> findAllRascunhos(
            @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageResponse = articleService.findAllArticlesRascunho(pageable);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(
            summary = "Buscar artigo por ID",
            description = "Retorna um artigo específico. Artigos PUBLICADOS podem ser visualizados por qualquer usuário autenticado. Artigos RASCUNHO só podem ser visualizados pelo autor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Artigo encontrado",
                    content = @Content(schema = @Schema(implementation = ArticleResponseWithTitleAndStatusAndCategoryName.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - rascunho de outro usuário"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseWithTitleAndStatusAndCategoryName> findById(
            @Parameter(description = "ID do artigo", example = "1", required = true)
            @PathVariable Long id
    ) {
        var response = articleService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Atualizar artigo",
            description = "Atualiza um artigo existente. Apenas o autor pode atualizar seus próprios artigos."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Artigo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ArticleResponseWithTitleAndStatusAndCategoryName.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - você só pode editar seus próprios artigos"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponseWithTitleAndStatusAndCategoryName> update(
            @Parameter(description = "ID do artigo", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody(
                    description = "Dados atualizados do artigo",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateArticleRequestDTO.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateArticleRequestDTO dto
    ) {
        var response = articleService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Deletar artigo",
            description = "Exclui um artigo. Apenas o autor pode excluir seus próprios artigos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artigo deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - você só pode excluir seus próprios artigos"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do artigo", example = "1", required = true)
            @PathVariable Long id
    ) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}