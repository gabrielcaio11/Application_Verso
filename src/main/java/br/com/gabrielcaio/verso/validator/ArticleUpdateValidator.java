package br.com.gabrielcaio.verso.validator;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.dtos.UpdateArticleRequestDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleUpdateValidator {

    private final ArticleRepository articleRepository;

    public void validate(Article article, UpdateArticleRequestDTO updated, User currentUser) {

        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Você só pode editar seus próprios artigos");
        }

        if (article.getStatus() == ArticleStatus.PUBLICADO &&
                "RASCUNHO".equalsIgnoreCase(updated.getStatus())) {
            throw new BusinessException("Não é possível alterar o status de um artigo publicado para rascunho");
        }

        if (updated.getStatus() != null) {
            try {
                ArticleStatus.valueOf(updated.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Status inválido: " + updated.getStatus());
            }
        }

        if (updated.getTitle() != null && !updated.getTitle().isBlank()) {
            boolean exists = articleRepository.existsByAuthorIdAndTitleIgnoreCase(
                    currentUser.getId(), updated.getTitle());
            if (exists && !updated.getTitle().equalsIgnoreCase(article.getTitle())) {
                throw new BusinessException("Você já possui um artigo com esse título");
            }
        }
    }
}

