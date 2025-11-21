package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.DataBaseException;
import br.com.gabrielcaio.verso.controllers.error.EntityExistsException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.dtos.CategoryDTO;
import br.com.gabrielcaio.verso.dtos.CategoryResponseWithNameDTO;
import br.com.gabrielcaio.verso.dtos.CreateCategoryRequestDTO;
import br.com.gabrielcaio.verso.dtos.UpdateCategoryRequestDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private static final String DEFAULT_CATEGORY_NAME = "Sem categoria";

  private final CategoryRepository categoryRepository;
  private final ArticleRepository articleRepository;

  // ---------------------------------------------------------
  // CREATE
  // ---------------------------------------------------------
  @Transactional
  public CategoryResponseWithNameDTO create(CreateCategoryRequestDTO request) {
    log.info("[CATEGORY CREATE] Recebida requisição para criar categoria: {}", request.getName());

    String name = request.getName().trim().toUpperCase();

    categoryRepository
        .findByName(name)
        .ifPresent(
            category -> {
              log.warn("[CATEGORY CREATE] Tentativa de criar categoria duplicada: {}", name);
              throw new EntityExistsException(
                  "Nome de categoria (" + category.getName() + ") já existe");
            });

    Category entity = Category.builder().name(name).build();
    entity = categoryRepository.save(entity);

    log.info(
        "[CATEGORY CREATE] Categoria criada com sucesso. ID: {}, Nome: {}",
        entity.getId(),
        entity.getName());
    return new CategoryResponseWithNameDTO(entity.getName());
  }

  // ---------------------------------------------------------
  // UPDATE
  // ---------------------------------------------------------
  @Transactional
  public CategoryResponseWithNameDTO update(Long id, UpdateCategoryRequestDTO request) {
    log.info(
        "[CATEGORY UPDATE] Atualizando categoria ID: {} para novo nome: {}", id, request.getName());

    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.warn("[CATEGORY UPDATE] Categoria ID {} não encontrada", id);
                  return new ResourceNotFoundException(
                      "Categoria não encontrada. Nome: " + request.getName());
                });

    String newName = request.getName().trim().toUpperCase();

    categoryRepository
        .findByName(newName)
        .ifPresent(
            existing -> {
              log.warn("[CATEGORY UPDATE] Nome de categoria já existente: {}", newName);
              throw new EntityExistsException("Nome de categoria já existente. Nome: " + newName);
            });

    category.setName(newName);
    category = categoryRepository.save(category);

    log.info(
        "[CATEGORY UPDATE] Categoria atualizada com sucesso. ID: {}, Novo nome: {}", id, newName);
    return new CategoryResponseWithNameDTO(category.getName());
  }

  // ---------------------------------------------------------
  // DELETE
  // ---------------------------------------------------------
  @Transactional
  public void delete(Long id) {

    log.info("[CATEGORY DELETE] Solicitada exclusão da categoria ID: {}", id);

    var categoryToDelete =
        categoryRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.warn("[CATEGORY DELETE] Categoria ID {} não encontrada para exclusão", id);
                  return new ResourceNotFoundException("Categoria não encontrada");
                });

    var categoryDefault = getOrCreateDefaultCategory();

    if (categoryDefault.getId().equals(id)) {
      log.warn("[CATEGORY DELETE] Tentativa de excluir categoria default (ID: {})", id);
      throw new DataBaseException("A categoria default não pode ser excluída");
    }

    List<Article> articles = articleRepository.findAllByCategory(categoryToDelete);
    log.info(
        "[CATEGORY DELETE] Reatribuindo {} artigos para categoria default ({})",
        articles.size(),
        categoryDefault.getName());

    articles.forEach(a -> a.setCategory(categoryDefault));
    articleRepository.saveAll(articles);

    try {
      categoryRepository.delete(categoryToDelete);
      log.info("[CATEGORY DELETE] Categoria ID {} excluída com sucesso", id);
    } catch (DataIntegrityViolationException e) {
      log.error("[CATEGORY DELETE] Falha ao excluir categoria ID {}. Erro: {}", id, e.getMessage());
      throw new DataBaseException("Falha de integridade referencial");
    }
  }

  // ---------------------------------------------------------
  // FIND BY ID
  // ---------------------------------------------------------
  @Transactional(readOnly = true)
  public CategoryResponseWithNameDTO findById(Long id) {
    log.info("[CATEGORY FIND BY ID] Buscando categoria ID {}", id);

    var category =
        categoryRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.warn("[CATEGORY FIND BY ID] Categoria ID {} não encontrada", id);
                  return new ResourceNotFoundException("Categoria não encontrada");
                });

    log.info("[CATEGORY FIND BY ID] Categoria encontrada: {}", category.getName());
    return new CategoryResponseWithNameDTO(category.getName());
  }

  // ---------------------------------------------------------
  // LIST ALL
  // ---------------------------------------------------------
  @Transactional(readOnly = true)
  public Page<CategoryResponseWithNameDTO> findAll(Pageable pageable) {
    log.info(
        "[CATEGORY FIND ALL] Listando categorias. Página: {}, Tamanho: {}",
        pageable.getPageNumber(),
        pageable.getPageSize());

    var categoriesPage = categoryRepository.findAll(pageable);

    log.info("[CATEGORY FIND ALL] Retornando {} categorias", categoriesPage.getNumberOfElements());

    return categoriesPage.map(category -> new CategoryResponseWithNameDTO(category.getName()));
  }

  // ---------------------------------------------------------
  // INTERNAL HELPERS
  // ---------------------------------------------------------
  private CategoryDTO toDTO(Category entity) {
    return new CategoryDTO(entity.getId(), entity.getName());
  }

  private Category getOrCreateDefaultCategory() {
    log.debug("[CATEGORY DEFAULT] Verificando existência da categoria default");

    return categoryRepository
        .findByName(DEFAULT_CATEGORY_NAME)
        .orElseGet(
            () -> {
              log.warn(
                  "[CATEGORY DEFAULT] Categoria default não encontrada. Criando automaticamente…");

              this.create(new CreateCategoryRequestDTO(DEFAULT_CATEGORY_NAME));

              return categoryRepository
                  .findByName(DEFAULT_CATEGORY_NAME)
                  .orElseThrow(
                      () -> {
                        log.error("[CATEGORY DEFAULT] Falha ao criar categoria default");
                        return new IllegalStateException("Falha ao criar categoria default");
                      });
            });
  }
}
