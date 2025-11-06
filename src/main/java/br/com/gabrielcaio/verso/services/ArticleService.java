package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.DataBaseException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.dtos.ArticleResponseWithTitleAndStatusAndCategoryName;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import br.com.gabrielcaio.verso.dtos.UpdateArticleRequestDTO;
import br.com.gabrielcaio.verso.mappers.ArticleMapper;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import br.com.gabrielcaio.verso.validator.ArticleCreateValidator;
import br.com.gabrielcaio.verso.validator.ArticleDeleteValidator;
import br.com.gabrielcaio.verso.validator.ArticleUpdateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final UserService userService;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final ArticleMapper articleMapper;
    private final ArticleUpdateValidator articleUpdateValidator;
    private final ArticleCreateValidator articleCreateValidator;
    private final ArticleDeleteValidator articleDeleteValidator;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<ArticleResponseWithTitleAndStatusAndCategoryName> findAllArticles(
            Pageable pageable) {
        var articlesPage = articleRepository.findAllByStatus(ArticleStatus.PUBLICADO, pageable);
        return articlesPage.map(articleMapper::toResponseWithTitleAndStatusAndCategoryName);
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponseWithTitleAndStatusAndCategoryName> findAllArticlesRascunho(
            Pageable pageable) {
        var currentUser = userService.getCurrentUser();
        var articlesPage = articleRepository.findAllByStatusAndAuthor(ArticleStatus.RASCUNHO, currentUser, pageable);
        return articlesPage.map(articleMapper::toResponseWithTitleAndStatusAndCategoryName);
    }

    @Transactional(readOnly = true)
    public ArticleResponseWithTitleAndStatusAndCategoryName findById(Long id) {
        var article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        var currentUser = userService.getCurrentUser();

        if (article.getStatus() == ArticleStatus.RASCUNHO &&
                !article.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Acesso negado a rascunhos de outros usuários");
        }

        return articleMapper.toResponseWithTitleAndStatusAndCategoryName(article);
    }

    @Transactional
    public ArticleResponseWithTitleAndStatusAndCategoryName update(Long id, UpdateArticleRequestDTO updated) {
        var article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));
        var currentUser = userService.getCurrentUser();

        articleUpdateValidator.validate(article, updated, currentUser);

        ArticleStatus oldStatus = article.getStatus();
        ArticleStatus newStatus = ArticleStatus.valueOf(updated.getStatus().toUpperCase());

        if (!updated.getTitle().isBlank()) {
            article.setTitle(updated.getTitle());
        }
        if (!updated.getContent().isBlank()) {
            article.setContent(updated.getContent());
        }
        if (!updated.getCategory().isBlank()) {
            var category = categoryRepository.findByName(updated.getCategory())
                    .orElse(cadastrarCategoria(updated.getCategory()));
            article.setCategory(category);
        }

        article.setStatus(newStatus);
        articleRepository.save(article);

        // Criar notificações quando um artigo é publicado (transição de rascunho para
        // publicado)
        if (oldStatus == ArticleStatus.RASCUNHO && newStatus == ArticleStatus.PUBLICADO) {
            notificationService.createNotificationForFollowers(article);
        }

        return articleMapper.toResponseWithTitleAndStatusAndCategoryName(article);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        var article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo com id " + id + " não encontrado"));
        var currentUser = userService.getCurrentUser();

        articleDeleteValidator.validate(article, currentUser);

        try {
            articleRepository.delete(article);
        } catch (DataIntegrityViolationException e) {
            throw new DataBaseException("Falha de integridade referencial");
        }
    }

    @Transactional
    public CreateArticleResponseDTO create(CreateArticleRequestDTO dto) {
        var article = articleMapper.toEntity(dto);
        var author = userService.getCurrentUser();
        var category = categoryRepository.findByName(dto.getCategory())
                .orElse(cadastrarCategoria(dto.getCategory()));

        articleCreateValidator.validate(article, author, category);

        article.setAuthor(author);
        author.getArticles().add(article);

        article.setCategory(category);
        category.getArticles().add(article);

        article = articleRepository.save(article);

        // Criar notificações se o artigo foi criado como publicado
        if (article.getStatus() == ArticleStatus.PUBLICADO) {
            notificationService.createNotificationForFollowers(article);
        }

        return articleMapper.toCreateResponse(article);
    }

    private Category cadastrarCategoria(String category) {
        if (categoryRepository.findByName(category).isPresent()) {
            return categoryRepository.findByName(category).get();
        }
        var novaCategoria = new Category();
        novaCategoria.setName(category);
        return categoryRepository.save(novaCategoria);
    }
}
