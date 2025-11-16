package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.NotificationResponseDTO;
import br.com.gabrielcaio.verso.services.NotificationService;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/verso/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints para gerenciamento de notificações")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Listar todas as notificações",
            description = "Retorna uma lista paginada de todas as notificações do usuário autenticado, ordenadas por data de criação (mais recentes primeiro)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de notificações retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @Parameters({
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
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @ParameterObject
            Pageable pageable
    ) {
        log.info("Buscando notificações do usuário autenticado. Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        var pageResponse = notificationService.getAllNotifications(pageable);
        log.info("Total de notificações encontradas: {}", pageResponse.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
    }

    @Operation(
            summary = "Listar notificações não lidas",
            description = "Retorna uma lista paginada de notificações não lidas do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de notificações não lidas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @Parameters({
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
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponseDTO>> getUnreadNotifications(
            @ParameterObject
            Pageable pageable
    ) {
        log.info("Buscando notificações não lidas do usuário autenticado. Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        var pageResponse = notificationService.getUnreadNotifications(pageable);
        log.info("Total de notificações não lidas encontradas: {}", pageResponse.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
    }

    @Operation(
            summary = "Marcar notificação como lida",
            description = "Marca uma notificação específica como lida."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificação marcada como lida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(
                    description = "ID da notificação",
                    example = "1",
                    required = true
            )
            @PathVariable Long notificationId
    ) {
        log.info("Marcando notificação {} como lida", notificationId);
        notificationService.markAsRead(notificationId);
        log.info("Notificação {} marcada como lida com sucesso", notificationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Marcar todas as notificações como lidas",
            description = "Marca todas as notificações do usuário autenticado como lidas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas as notificações marcadas como lidas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        log.info("Marcando todas as notificações como lidas para o usuário autenticado");
        notificationService.markAllAsRead();
        log.info("Todas as notificações marcadas como lidas com sucesso");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Contar notificações não lidas",
            description = "Retorna o número de notificações não lidas do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Contagem de notificações não lidas retornada com sucesso"
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        log.info("Contando notificações não lidas para o usuário autenticado");
        long count = notificationService.getUnreadCount();
        log.info("Número de notificações não lidas: {}", count);
        return ResponseEntity.status(HttpStatus.OK).body(count);
    }
}