package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import br.com.gabrielcaio.verso.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class ArticleServiceIntegrationTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private Category category;

    @BeforeEach
    void setup() {
        author = new User();
        author.setName("Gabriel");
        author.setEmail("gabriel@example.com");
        userRepository.save(author);

        category = new Category();
        category.setName("Tecnologia");
        categoryRepository.save(category);
    }

    @Test
    void deveSalvarERecuperarArtigoDoBanco() {
        var article = Article.builder()
                .title("Título de Teste")
                .content("Conteúdo de Teste")
                .status(ArticleStatus.PUBLICADO)
                .author(author)
                .category(category)
                .build();

        articleRepository.save(article);

        var encontrado = articleRepository.findById(article.getId()).orElse(null);

        assertNotNull(encontrado);
        assertEquals("Título de Teste", encontrado.getTitle());
    }

    @Test
    void deveAtualizarStatusDoArtigo() {
        var article = Article.builder()
                .title("Artigo Draft")
                .content("Conteúdo")
                .status(ArticleStatus.RASCUNHO)
                .author(author)
                .category(category)
                .build();

        article = articleRepository.save(article);
        article.setStatus(ArticleStatus.PUBLICADO);
        articleRepository.save(article);

        var atualizado = articleRepository.findById(article.getId()).orElse(null);
        assertEquals(ArticleStatus.PUBLICADO, atualizado.getStatus());
    }
}
