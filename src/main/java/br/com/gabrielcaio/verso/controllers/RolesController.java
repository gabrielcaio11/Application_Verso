package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.CreateRolesRequestDTO;
import br.com.gabrielcaio.verso.dtos.RolesWithIdAndName;
import br.com.gabrielcaio.verso.services.RolesService;
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
@RequestMapping("/verso/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Endpoints para gerenciamento de roles")
@Slf4j
public class RolesController
{

    private final RolesService rolesService;

    @Operation(
            summary = "Buscar todos as roles com paginação",
            description = "Retorna uma lista paginada de roles."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de roles retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
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
    public ResponseEntity<Page<RolesWithIdAndName>> getAll(
            @ParameterObject
            Pageable pageable
    )
    {
        log.info(
                "Buscando todos as roles. Página: {}, Tamanho: {}", pageable.getPageNumber(),
                pageable.getPageSize()
        );
        var pageResponse = rolesService.findAllRolesWithIdAndName(pageable);
        log.info(
                "Roles recuperadas com sucesso. Total de roles: {}",
                pageResponse.getTotalElements()
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(pageResponse);
    }

    @Operation(
            summary = "Buscar role por ID",
            description = "Retorna uma role específica."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role encontrada",
                    content = @Content(schema = @Schema(implementation = RolesWithIdAndName.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Role não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RolesWithIdAndName> getById(
            @Parameter(
                    description = "ID da role",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    )
    {
        log.info("Buscando role por ID: {}", id);
        var response = rolesService.findById(id);
        log.info("Role recuperada com sucesso: {}", response.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "Registro de nova role",
            description = "Cria um nova role."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role criada com sucesso",
                    content = @Content(schema = @Schema(implementation = RolesWithIdAndName.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado - autenticação necessária"),
            @ApiResponse(responseCode = "422", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<RolesWithIdAndName> create(
            @RequestBody CreateRolesRequestDTO role
    )
    {
        log.info("Criando nova role: {}", role.getName());
        var response = rolesService.save(role);
        log.info("Role criada com sucesso: {}", response.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Atualizar role",
            description = "Atualiza uma role existente."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = RolesWithIdAndName.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RolesWithIdAndName> update(
            @Parameter(
                    description = "ID da role",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @RequestBody CreateRolesRequestDTO dto
    )
    {
        log.info("Atualizando role com ID: {}", id);
        var updatedRole = rolesService.update(id, dto);
        log.info("Role com ID: {} atualizada com sucesso", id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(updatedRole);
    }

    @Operation(
            summary = "Deletar role",
            description = "Exclui uma role. Apenas usario com papel de ADMIN pode excluir roles."
    )
    @ApiResponses( {
            @ApiResponse(responseCode = "204", description = "Role deletada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode deletar roles"),
            @ApiResponse(responseCode = "404", description = "Role não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "ID da role",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    )
    {
        log.info("Deletando role com ID: {}", id);
        rolesService.delete(id);
        log.info("Role com ID: {} deletada com sucesso", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}
