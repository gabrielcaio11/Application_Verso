package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.domain.entity.Roles;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import br.com.gabrielcaio.verso.mappers.ArticleMapper;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import br.com.gabrielcaio.verso.validator.ArticleCreateValidator;
import br.com.gabrielcaio.verso.validator.ArticleDeleteValidator;
import br.com.gabrielcaio.verso.validator.ArticleUpdateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private ArticleUpdateValidator articleUpdateValidator;
    @Mock
    private ArticleCreateValidator articleCreateValidator;
    @Mock
    private ArticleDeleteValidator articleDeleteValidator;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ArticleService articleService;

    private User author;
    private Category category;
    private Article article;

    @BeforeEach
    void setup() {

        Roles roleUser = new Roles();
        roleUser.setId(1L);
        roleUser.setName("USER");

        author = new User();
        author.setId(1L);
        author.setUsername("gabriel");
        author.setEmail("gabriel@gmail.com");

        category = new Category();
        category.setId(1L);
        category.setName("Tecnologia");

        article = Article.builder()
                .id(1L)
                .title("Artigo Teste")
                .content("Conteúdo de teste")
                .status(ArticleStatus.PUBLICADO)
                .author(author)
                .category(category)
                .build();
    }

    @Test
    void deveRetornarArtigoPorIdQuandoExistir() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userService.getCurrentUser()).thenReturn(author);
        when(articleMapper.toResponseWithTitleAndStatusAndCategoryName(article)).thenReturn(null);

        var resultado = articleService.findById(1L);

        assertNotNull(resultado);
        verify(articleRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoArtigoNaoExistir() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> articleService.findById(99L));
    }

    @Test
    void deveCriarArtigoComSucesso() {
        var dto = new CreateArticleRequestDTO("Artigo Teste", "Conteúdo", "Tecnologia", "PUBLICADO");
        when(userService.getCurrentUser()).thenReturn(author);
        when(categoryRepository.findByName("Tecnologia")).thenReturn(Optional.of(category));
        when(articleMapper.toEntity(dto)).thenReturn(article);
        when(articleRepository.save(article)).thenReturn(article);
        when(articleMapper.toCreateResponse(article)).thenReturn(new CreateArticleResponseDTO("Artigo Teste", "Conteúdo", "Tecnologia"));

        var response = articleService.create(dto);

        assertNotNull(response);
        verify(articleCreateValidator).validate(any(), any(), any());
        verify(notificationService).createNotificationForFollowers(article);
    }

    @Test
    void deveLancarExcecaoAoExcluirArtigoInexistente() {
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> articleService.delete(999L));
    }

    @Test
    void deveLancarExcecaoDeIntegridadeAoExcluirArtigo() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userService.getCurrentUser()).thenReturn(author);
        doThrow(new DataIntegrityViolationException("violação")).when(articleRepository).delete(article);

        assertThrows(RuntimeException.class, () -> articleService.delete(1L));
    }
}
