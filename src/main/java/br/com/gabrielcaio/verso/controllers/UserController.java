package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.UserDTO;
import br.com.gabrielcaio.verso.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/verso/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints para gerenciamento de usuários")
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Registro de novo usuário",
            description = "Cria um novo usuário no sistema. O email deve ser único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "422", description = "Erro de validação - email já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<Void> register(
            @RequestBody(
                    description = "Dados do usuário a ser criado",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody UserDTO dto) {
        log.info("Registrando novo usuário com email: {}", dto.getEmail());
        userService.register(dto);
        log.info("Usuário registrado com sucesso com email: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Buscar todos os usuários",
            description = "Retorna uma lista de todos os usernames cadastrados. Apenas usuários com perfil ADMIN podem acessar."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode listar usuários"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<String>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("Buscando todos os usuários. Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        var pageResponse = userService.findAll(pageable);
        log.info("Total de usuários encontrados: {}", pageResponse.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
    }
}