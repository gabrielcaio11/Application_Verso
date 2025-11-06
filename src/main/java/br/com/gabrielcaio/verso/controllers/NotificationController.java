package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.NotificationResponseDTO;
import br.com.gabrielcaio.verso.services.NotificationService;
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
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageResponse = notificationService.getAllNotifications(pageable);
        return ResponseEntity.ok(pageResponse);
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
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponseDTO>> getUnreadNotifications(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageResponse = notificationService.getUnreadNotifications(pageable);
        return ResponseEntity.ok(pageResponse);
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
            @Parameter(description = "ID da notificação", example = "1", required = true)
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
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
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
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
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(count);
    }
}