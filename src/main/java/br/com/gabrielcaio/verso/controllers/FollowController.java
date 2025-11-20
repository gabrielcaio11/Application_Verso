package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.FollowResponseDTO;
import br.com.gabrielcaio.verso.dtos.UserProfileDTO;
import br.com.gabrielcaio.verso.services.FollowService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verso/follows")
@RequiredArgsConstructor
@Tag(name = "Follows", description = "Endpoints para gerenciamento de seguidores")
@Slf4j
public class FollowController
{

    private final FollowService followService;

    @Operation(
            summary = "Seguir um usuário",
            description = "Inicia o seguimento de um usuário. Você receberá notificações quando esse usuário publicar novos artigos."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário seguido com sucesso",
                    content = @Content(schema = @Schema(implementation = FollowResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Não é possível seguir a si mesmo"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "422", description = "Você já está seguindo este usuário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<FollowResponseDTO> followUser(
            @Parameter(
                    description = "ID do usuário a ser seguido",
                    example = "1",
                    required = true
            )
            @PathVariable Long userId
    )
    {
        log.info("Request to follow user with ID: {}", userId);
        var response = followService.followUser(userId);
        log.info("Successfully followed user with ID: {}", userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "Deixar de seguir um usuário",
            description = "Remove o seguimento de um usuário. Você não receberá mais notificações desse usuário."
    )
    @ApiResponses( {
            @ApiResponse(responseCode = "204", description = "Usuário deixado de seguir com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado ou você não está seguindo este usuário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unfollowUser(
            @Parameter(description = "ID do usuário a ser deixado de seguir", example = "1", required = true)
            @PathVariable Long userId
    )
    {
        log.info("Request to unfollow user with ID: {}", userId);
        followService.unfollowUser(userId);
        log.info("Successfully unfollowed user with ID: {}", userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @Operation(
            summary = "Listar usuários que você está seguindo",
            description = "Retorna uma lista paginada de usuários que o usuário autenticado está seguindo."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários seguidos retornada com sucesso",
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
    @GetMapping("/following")
    public ResponseEntity<Page<UserProfileDTO>> getFollowing(
            @ParameterObject
            Pageable pageable
    )
    {
        log.info(
                "Buscando lista de quem o usuário autenticado segue. Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize()
        );
        var pageResponse = followService.getFollowing(pageable);
        log.info(
                "Lista de usuários seguidos retornada com sucesso. Total de elementos: {}",
                pageResponse.getTotalElements()
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(pageResponse);
    }

    @Operation(
            summary = "Listar seus seguidores",
            description = "Retorna uma lista paginada de usuários que seguem o usuário autenticado."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de seguidores retornada com sucesso",
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
    @GetMapping("/followers")
    public ResponseEntity<Page<UserProfileDTO>> getFollowers(
            @ParameterObject
            Pageable pageable
    )
    {
        log.info(
                "Buscando lista de seguidores do usuário autenticado. Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize()
        );
        var pageResponse = followService.getFollowers(pageable);
        log.info(
                "Lista de seguidores retornada com sucesso. Total de elementos: {}",
                pageResponse.getTotalElements()
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(pageResponse);
    }

    @Operation(
            summary = "Ver perfil de um usuário",
            description = "Retorna informações do perfil de um usuário, incluindo contagem de seguidores e se você está seguindo."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil do usuário retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @Parameter(
                    description = "ID do usuário",
                    example = "1",
                    required = true
            )
            @PathVariable Long userId
    )
    {
        log.info("Buscando perfil do usuário com ID: {}", userId);
        var profile = followService.getUserProfile(userId);
        log.info("Perfil do usuário com ID: {} retornado com sucesso", userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(profile);
    }

    @Operation(
            summary = "Verificar se está seguindo um usuário",
            description = "Verifica se o usuário autenticado está seguindo um usuário específico."
    )
    @ApiResponses( {
            @ApiResponse(
                    responseCode = "200",
                    description = "Status do seguimento retornado com sucesso"
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{userId}/check")
    public ResponseEntity<Boolean> checkFollowing(
            @Parameter(
                    description = "ID do usuário",
                    example = "1",
                    required = true
            )
            @PathVariable Long userId
    )
    {
        log.info("Verificando se o usuário autenticado está seguindo o usuário com ID: {}", userId);
        boolean isFollowing = followService.isFollowing(userId);
        log.info("Status de seguimento para o usuário com ID: {} é {}", userId, isFollowing);
        return ResponseEntity.status(HttpStatus.OK)
                .body(isFollowing);
    }
}