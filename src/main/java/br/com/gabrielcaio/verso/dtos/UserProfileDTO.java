package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO com informações básicas do perfil de usuário")
public class UserProfileDTO {
    
    @Schema(
        description = "ID do usuário",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;
    
    @Schema(
        description = "Username do usuário",
        example = "joao",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;
    
    @Schema(
        description = "Email do usuário",
        example = "joao@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;
    
    @Schema(
        description = "Número de seguidores",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long followersCount;
    
    @Schema(
        description = "Número de pessoas que o usuário está seguindo",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long followingCount;
    
    @Schema(
        description = "Se o usuário autenticado está seguindo este usuário",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isFollowing;
}