package br.com.gabrielcaio.verso.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ThreadedCommentDTO {
    private Long id;
    private String content;
    private Long authorId;
    private String authorUsername;
    private Long articleId;
    private LocalDateTime createdAt;
    private List<ThreadedCommentDTO> replies;
}
