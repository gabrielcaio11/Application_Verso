package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.DataBaseException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.dtos.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final UserService userService;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final ArticleMapper articleMapper;
    private final ArticleUpdateValidator articleUpdateValidator;
    private final ArticleCreateValidator articleCreateValidator;
    private final ArticleDeleteValidator articleDeleteValidator;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<ArticleResponseWithTitleAndStatusAndCategoryName> findAllArticlesPublicados(
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

        if (article.getStatus() == ArticleStatus.PUBLICADO) {
            return articleMapper.toResponseWithTitleAndStatusAndCategoryName(article);
        }
        if (article.getStatus() == ArticleStatus.RASCUNHO) {
            var currentUser = userService.getCurrentUser();
            if (article.getAuthor().getId().equals(currentUser.getId())) {
                return articleMapper.toResponseWithTitleAndStatusAndCategoryName(article);
            }
        }
        throw new ResourceNotFoundException("O Autor não tem artigo com esse id");
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

        if (article.getStatus() == ArticleStatus.PUBLICADO) {
            notificationService.createNotificationForFollowers(article);
        }

        return articleMapper.toCreateResponse(article);
    }

    private Category cadastrarCategoria(String category) {
        var categoryDTO = categoryService.create(new CreateCategoryRequestDTO(category));
        return categoryRepository.findByName(categoryDTO.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada após criação"));
    }
}
