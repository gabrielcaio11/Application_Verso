package br.com.gabrielcaio.verso.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentRequestDTO {
    @NotBlank
    @Size(min = 1, max = 5000)
    private String content;

    private Long parentId;
}
