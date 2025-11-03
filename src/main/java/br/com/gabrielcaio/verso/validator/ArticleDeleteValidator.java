package br.com.gabrielcaio.verso.validator;

import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleDeleteValidator {
    public void validate(Article article, User currentUser) {
        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Você só pode excluir seus próprios artigos");
        }
    }
}