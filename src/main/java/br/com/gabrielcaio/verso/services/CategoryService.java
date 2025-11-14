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
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String DEFAULT_CATEGORY_NAME = "Sem categoria";

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public CategoryResponseWithNameDTO create(CreateCategoryRequestDTO request) {
        String name = request.getName().trim();

        categoryRepository.findByName(name).ifPresent(category -> {
            throw new DataBaseException("Nome de categoria (" + category.getName() + ") já existe");
        });

        Category entity = Category.builder().name(name).build();
        entity = categoryRepository.save(entity);
        return new CategoryResponseWithNameDTO(entity.getName());
    }

    @Transactional
    public CategoryResponseWithNameDTO update(Long id, UpdateCategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. Nome: " + request.getName()));

        String newName = request.getName().trim();
        categoryRepository.findByName(newName).ifPresent(existing -> {
            throw new EntityExistsException("Nome de categoria já existente. Nome: " + newName);
        });

        category.setName(newName);
        category = categoryRepository.save(category);
        return new CategoryResponseWithNameDTO(category.getName());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        var categoryToDelete = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        var optionalCategoryDefault = categoryRepository.findByName(DEFAULT_CATEGORY_NAME);

        if (optionalCategoryDefault.isEmpty()) {
            this.create(new CreateCategoryRequestDTO(DEFAULT_CATEGORY_NAME));
        }

        if (optionalCategoryDefault.get().getId().equals(id)) {
            throw new DataBaseException("A categoria default não pode ser excluída");
        }

        List<Article> articles = articleRepository.findAllByCategory(categoryToDelete);
        for (Article a : articles) {
            a.setCategory(optionalCategoryDefault.get());
        }
        articleRepository.saveAll(articles);

        try {
            categoryRepository.delete(categoryToDelete);
        } catch (DataIntegrityViolationException e) {
            throw new DataBaseException("Falha de integridade referencial");
        }
    }

    @Transactional(readOnly = true)
    public CategoryResponseWithNameDTO findById(Long id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        return new CategoryResponseWithNameDTO(category.getName());
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponseWithNameDTO> findAll(Pageable pageable) {
        var categoriesPage = categoryRepository.findAll(pageable);
        return categoriesPage.map(category -> new CategoryResponseWithNameDTO(category.getName()));
    }

    private CategoryDTO toDTO(Category entity) {
        return new CategoryDTO(entity.getId(), entity.getName());
    }
}