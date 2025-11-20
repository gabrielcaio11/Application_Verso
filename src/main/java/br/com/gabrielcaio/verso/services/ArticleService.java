package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService
{

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
            Pageable pageable
    )
    {
        log.info(
                "Buscando artigos PUBLICADOS com paginação page={} size={}",
                pageable.getPageNumber(), pageable.getPageSize()
        );

        var articlesPage = articleRepository.findAllByStatus(ArticleStatus.PUBLICADO, pageable);

        log.info("Encontrados {} artigos publicados", articlesPage.getTotalElements());
        return articlesPage.map(articleMapper::toResponseWithTitleAndStatusAndCategoryName);
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponseWithTitleAndStatusAndCategoryName> findAllArticlesRascunho(
            Pageable pageable
    )
    {

        var currentUser = userService.getCurrentUser();
        log.info(
                "Buscando rascunhos do usuário id={} username={}", currentUser.getId(),
                currentUser.getUsername()
        );

        var articlesPage = articleRepository.findAllByStatusAndAuthor(
                ArticleStatus.RASCUNHO, currentUser, pageable);

        log.info(
                "Encontrados {} artigos rascunho do usuário {}", articlesPage.getTotalElements(),
                currentUser.getUsername()
        );
        return articlesPage.map(articleMapper::toResponseWithTitleAndStatusAndCategoryName);
    }

    @Transactional(readOnly = true)
    public ArticleResponseWithTitleAndStatusAndCategoryName findById(Long id)
    {

        log.info("Buscando artigo por id={}", id);

        var article = articleRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.warn("Artigo id={} não encontrado", id);
                    return new ResourceNotFoundException("Artigo não encontrado");
                });

        if(article.getStatus() == ArticleStatus.PUBLICADO)
        {
            log.info("Artigo id={} retornado (status PUBLICADO)", id);
            return articleMapper.toResponseWithTitleAndStatusAndCategoryName(article);
        }

        var currentUser = userService.getCurrentUser();
        log.debug("Artigo id={} é RASCUNHO, verificando autorização. Autor={}, Usuário Atual={}",
                id, article.getAuthor()
                        .getId(), currentUser.getId()
        );

        if(article.getAuthor()
                .getId()
                .equals(currentUser.getId()))
        {
            log.info("Acesso permitido ao rascunho id={}", id);
            return articleMapper.toResponseWithTitleAndStatusAndCategoryName(article);
        }

        log.warn("Usuário id={} tentou acessar rascunho que não é dele (artigo id={})",
                currentUser.getId(), id
        );
        throw new ResourceNotFoundException("O Autor não tem artigo com esse id");
    }

    @Transactional
    public ArticleResponseWithTitleAndStatusAndCategoryName update(
            Long id, UpdateArticleRequestDTO updated
    )
    {

        log.info("Iniciando atualização do artigo id={}", id);

        var article = articleRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.warn("Artigo id={} não encontrado para atualização", id);
                    return new ResourceNotFoundException("Artigo não encontrado");
                });

        var currentUser = userService.getCurrentUser();
        log.debug("Validando atualização. Artigo={}, Usuário={}", id, currentUser.getUsername());

        articleUpdateValidator.validate(article, updated, currentUser);

        ArticleStatus oldStatus = article.getStatus();
        ArticleStatus newStatus = ArticleStatus.valueOf(updated.getStatus()
                .toUpperCase());

        log.debug("Status antigo={}, novo={}", oldStatus, newStatus);

        if(!updated.getTitle()
                .isBlank())
        {
            log.debug("Atualizando título para '{}'", updated.getTitle());
            article.setTitle(updated.getTitle());
        }

        if(!updated.getContent()
                .isBlank())
        {
            log.debug("Atualizando conteúdo");
            article.setContent(updated.getContent());
        }

        if(!updated.getCategory()
                .isBlank())
        {
            var categoryName = updated.getCategory()
                    .toUpperCase();
            log.debug("Atualizando categoria para '{}'", categoryName);

            var category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> safeCreateCategory(categoryName));

            article.setCategory(category);
        }

        article.setStatus(newStatus);
        articleRepository.save(article);

        log.info("Artigo id={} atualizado com sucesso", id);

        if(oldStatus == ArticleStatus.RASCUNHO && newStatus == ArticleStatus.PUBLICADO)
        {
            log.info("Artigo id={} publicado! Enviando notificações...", id);
            notificationService.createNotificationForFollowers(article);
        }

        return articleMapper.toResponseWithTitleAndStatusAndCategoryName(article);
    }

    @Transactional
    public void delete(Long id)
    {

        log.info("Iniciando exclusão do artigo id={}", id);

        var article = articleRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.warn("Tentativa de excluir artigo inexistente id={}", id);
                    return new ResourceNotFoundException("Artigo com id " + id + " não encontrado");
                });

        var currentUser = userService.getCurrentUser();
        log.debug(
                "Validando permissão para excluir artigo id={} do usuário {}", id,
                currentUser.getUsername()
        );

        articleDeleteValidator.validate(article, currentUser);

        try
        {
            articleRepository.delete(article);
            log.info("Artigo id={} excluído com sucesso", id);
        } catch(DataIntegrityViolationException e)
        {
            log.error("Erro de integridade ao excluir artigo id={}", id);
            throw new DataBaseException("Falha de integridade referencial");
        }
    }

    @Transactional
    public CreateArticleResponseDTO create(CreateArticleRequestDTO dto)
    {

        log.info("Iniciando criação de artigo={}", dto);

        var article = articleMapper.toEntity(dto);
        log.debug("Artigo mapeado parcialmente (sem autor/categoria)");

        var author = userService.getCurrentUser();
        log.debug("Autor identificado: id={} username={}", author.getId(), author.getUsername());

        String categoryName = dto.getCategory()
                .trim()
                .toUpperCase();
        log.debug("Processando categoria '{}'", categoryName);

        var category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> safeCreateCategory(categoryName));

        log.debug("Categoria final associada: {}", category.getName());

        articleCreateValidator.validate(article, author, category);
        log.debug("Validações concluídas para criação de artigo");

        article.setAuthor(author);
        article.setCategory(category);
        article.setStatus(parseStatus(dto.getStatus()));

        article = articleRepository.save(article);
        log.info("Artigo criado com sucesso id={} status={}", article.getId(), article.getStatus());

        if(article.getStatus() == ArticleStatus.PUBLICADO)
        {
            log.info("Enviando notificações para seguidores do autor {}", author.getUsername());
            notificationService.createNotificationForFollowers(article);
        }

        return articleMapper.toCreateResponse(article);
    }

    private ArticleStatus parseStatus(String rawStatus)
    {
        try
        {
            return ArticleStatus.valueOf(rawStatus.trim()
                    .toUpperCase());
        } catch(Exception e)
        {
            log.error("Status inválido recebido: '{}'", rawStatus);
            throw new BusinessException("Status inválido: " + rawStatus);
        }
    }

    private Category safeCreateCategory(String name)
    {
        log.debug("Criando categoria '{}'", name);
        try
        {
            var category = Category.builder()
                    .name(name)
                    .build();
            return categoryRepository.save(category);
        } catch(DataIntegrityViolationException e)
        {
            log.warn("Categoria '{}' já existia durante criação concorrente", name);
            return categoryRepository.findByName(name)
                    .orElseThrow(() -> new DataBaseException("Falha ao criar categoria"));
        }
    }
}
