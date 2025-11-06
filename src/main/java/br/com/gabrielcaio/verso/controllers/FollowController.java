package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.FollowResponseDTO;
import br.com.gabrielcaio.verso.dtos.UserProfileDTO;
import br.com.gabrielcaio.verso.services.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verso/follows")
@RequiredArgsConstructor
@Tag(name = "Follows", description = "Endpoints para gerenciamento de seguidores")
public class FollowController {

    private final FollowService followService;

    @Operation(
            summary = "Seguir um usuário",
            description = "Inicia o seguimento de um usuário. Você receberá notificações quando esse usuário publicar novos artigos."
    )
    @ApiResponses({
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
            @Parameter(description = "ID do usuário a ser seguido", example = "1", required = true)
            @PathVariable Long userId
    ) {
        var response = followService.followUser(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Deixar de seguir um usuário",
            description = "Remove o seguimento de um usuário. Você não receberá mais notificações desse usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário deixado de seguir com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado ou você não está seguindo este usuário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unfollowUser(
            @Parameter(description = "ID do usuário a ser deixado de seguir", example = "1", required = true)
            @PathVariable Long userId
    ) {
        followService.unfollowUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar usuários que você está seguindo",
            description = "Retorna uma lista paginada de usuários que o usuário autenticado está seguindo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários seguidos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/following")
    public ResponseEntity<Page<UserProfileDTO>> getFollowing(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageResponse = followService.getFollowing(pageable);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(
            summary = "Listar seus seguidores",
            description = "Retorna uma lista paginada de usuários que seguem o usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de seguidores retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/followers")
    public ResponseEntity<Page<UserProfileDTO>> getFollowers(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageResponse = followService.getFollowers(pageable);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(
            summary = "Ver perfil de um usuário",
            description = "Retorna informações do perfil de um usuário, incluindo contagem de seguidores e se você está seguindo."
    )
    @ApiResponses({
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
            @Parameter(description = "ID do usuário", example = "1", required = true)
            @PathVariable Long userId
    ) {
        var profile = followService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @Operation(
            summary = "Verificar se está seguindo um usuário",
            description = "Verifica se o usuário autenticado está seguindo um usuário específico."
    )
    @ApiResponses({
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
            @Parameter(description = "ID do usuário", example = "1", required = true)
            @PathVariable Long userId
    ) {
        boolean isFollowing = followService.isFollowing(userId);
        return ResponseEntity.ok(isFollowing);
    }
}