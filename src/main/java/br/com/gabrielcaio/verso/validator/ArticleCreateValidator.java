package br.com.gabrielcaio.verso.validator;

import br.com.gabrielcaio.verso.controllers.error.EntityExistsException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCreateValidator {
  private final ArticleRepository articleRepository;

  public void validate(Article article, User author, Category category) {
    boolean exists =
        articleRepository.existsByAuthorIdAndTitleIgnoreCase(author.getId(), article.getTitle());
    if (exists) {
      throw new EntityExistsException(
          "Author(" + author.getUsername() + ") já possui um artigo com esse título");
    }

    // validar status
    if (article.getStatus() == null) {
      article.setStatus(ArticleStatus.RASCUNHO);
    }
  }
}
