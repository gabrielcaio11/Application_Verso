package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.controllers.error.ErrorMessage;
import br.com.gabrielcaio.verso.dtos.ArticleResponseWithTitleAndStatusAndCategoryName;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import br.com.gabrielcaio.verso.dtos.UpdateArticleRequestDTO;
import br.com.gabrielcaio.verso.services.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verso/articles")
@RequiredArgsConstructor
@Tag(name = "Articles", description = "Article management APIs")
@Slf4j
public class ArticleController
{

    private final ArticleService articleService;

    @Operation(
            summary = "Create new article",
            description = "Creates a new article. Authenticated user will be set as author automatically."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "201",
                    description = "Article created successfully",
                    content = @Content(schema = @Schema(implementation = CreateArticleResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @PostMapping
    public ResponseEntity<CreateArticleResponseDTO> create(
            @Valid @RequestBody CreateArticleRequestDTO dto
    )
    {
        log.info("Creating article with title={} and status={}", dto.getTitle(), dto.getStatus());
        var response = articleService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Buscar todos os artigos publicados com paginação",
            description = "Retorna uma lista paginada de artigos com status PUBLICADO. Apenas artigos publicados são visíveis."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de artigos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @Parameters( {
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "page",
                    description = "Número da página (inicia em 0). Padrão: 0",
                    example = "0",
                    schema = @Schema(type = "integer", defaultValue = "0")
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "size",
                    description = "Quantidade de itens por página. Padrão: 10",
                    example = "10",
                    schema = @Schema(type = "integer", defaultValue = "10")
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "sort",
                    description = "Ordenação no formato: campo,(asc|desc). Padrão: createdAt,desc",
                    examples = {
                            @ExampleObject(name = "Ordenação por Data de criação", value = "createdAt,DESC"),
                            @ExampleObject(name = "Ordenação por ultima atualização", value = "updatedAt,DESC")
                    },
                    schema = @Schema(type = "string", defaultValue = "createdAt,DESC")
            )
    })
    @GetMapping
    public ResponseEntity<Page<ArticleResponseWithTitleAndStatusAndCategoryName>> findAllPublished(
            @ParameterObject
            Pageable pageable
    )
    {
        log.info(
                "Buscando todos os artigos publicados. Página={}, Tamanho={}",
                pageable.getPageNumber(), pageable.getPageSize()
        );
        var pageResponse = articleService.findAllArticlesPublicados(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(pageResponse);
    }

    @Operation(
            summary = "Buscar artigos em rascunho do usuário autenticado",
            description = "Retorna uma lista paginada de artigos com status RASCUNHO do usuário autenticado. Apenas o autor pode ver seus próprios rascunhos."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de rascunhos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @Parameters( {
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "page",
                    description = "Número da página (inicia em 0). Padrão: 0",
                    example = "0",
                    schema = @Schema(type = "integer", defaultValue = "0")
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "size",
                    description = "Quantidade de itens por página. Padrão: 10",
                    example = "10",
                    schema = @Schema(type = "integer", defaultValue = "10")
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "sort",
                    description = "Ordenação no formato: campo,(asc|desc). Padrão: createdAt,desc",
                    examples = {
                            @ExampleObject(name = "Ordenação por Data de criação", value = "createdAt,DESC"),
                            @ExampleObject(name = "Ordenação por ultima atualização", value = "updatedAt,DESC")
                    },
                    schema = @Schema(type = "string", defaultValue = "createdAt,DESC")
            )
    })
    @GetMapping("/drafts")
    public ResponseEntity<Page<ArticleResponseWithTitleAndStatusAndCategoryName>> findAllDrafts(
            @ParameterObject
            Pageable pageable
    )
    {
        log.info(
                "Buscando rascunhos do usuário autenticado. Página={}, Tamanho={}",
                pageable.getPageNumber(), pageable.getPageSize()
        );
        var pageResponse = articleService.findAllArticlesRascunho(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(pageResponse);
    }

    @Operation(
            summary = "Buscar artigo por ID",
            description = "Retorna um artigo específico. Artigos PUBLICADOS podem ser visualizados por qualquer usuário autenticado. Artigos RASCUNHO só podem ser visualizados pelo autor."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Artigo encontrado",
                    content = @Content(schema = @Schema(implementation = ArticleResponseWithTitleAndStatusAndCategoryName.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - rascunho de outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Artigo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseWithTitleAndStatusAndCategoryName> findById(
            @Parameter(
                    description = "ID do artigo",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    )
    {
        log.info("Buscando artigo por ID={}", id);
        var response = articleService.findById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "Atualizar artigo",
            description = "Atualiza um artigo existente. Apenas o autor pode atualizar seus próprios artigos."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Artigo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ArticleResponseWithTitleAndStatusAndCategoryName.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - você só pode editar seus próprios artigos",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Artigo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Erro de validação",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponseWithTitleAndStatusAndCategoryName> update(
            @Parameter(
                    description = "ID do artigo",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequestDTO dto
    )
    {
        log.info("Atualizando artigo id={} com novos dados", id);
        var response = articleService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "Deletar artigo",
            description = "Exclui um artigo. Apenas o autor pode excluir seus próprios artigos."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "204",
                    description = "Artigo deletado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - você só pode excluir seus próprios artigos",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Artigo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "ID do artigo",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    )
    {
        log.info("Requisição para deletar artigo id={}", id);
        articleService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}