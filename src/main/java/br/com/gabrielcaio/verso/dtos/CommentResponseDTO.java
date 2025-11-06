package br.com.gabrielcaio.verso.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponseDTO {
    private Long id;
    private String content;
    private Long authorId;
    private String authorUsername;
    private Long articleId;
    private Long parentId;
    private LocalDateTime createdAt;
}
