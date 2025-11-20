package br.com.gabrielcaio.verso.dtos;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentResponseDTO
{
    private Long id;
    private String content;
    private Long authorId;
    private String authorUsername;
    private Long articleId;
    private Long parentId;
    private LocalDateTime createdAt;
}
