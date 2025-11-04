package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.DataBaseException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.dtos.CategoryDTO;
import br.com.gabrielcaio.verso.dtos.CreateCategoryRequestDTO;
import br.com.gabrielcaio.verso.dtos.UpdateCategoryRequestDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    public CategoryDTO create(CreateCategoryRequestDTO request) {
        String name = request.getName().trim();

        categoryRepository.findByName(name).ifPresent(c -> {
            throw new DataBaseException("Nome de categoria já existente");
        });

        Category entity = Category.builder().name(name).build();
        entity = categoryRepository.save(entity);
        return toDTO(entity);
    }

    @Transactional
    public CategoryDTO update(Long id, UpdateCategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        String newName = request.getName().trim();
        categoryRepository.findByName(newName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DataBaseException("Nome de categoria já existente");
            }
        });

        category.setName(newName);
        category = categoryRepository.save(category);
        return toDTO(category);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        Category toDelete = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        Category defaultCategory = categoryRepository.findByName(DEFAULT_CATEGORY_NAME)
                .orElseGet(() -> categoryRepository.save(Category.builder().name(DEFAULT_CATEGORY_NAME).build()));

        if (toDelete.getId().equals(defaultCategory.getId())) {
            throw new DataBaseException("A categoria padrão não pode ser excluída");
        }

        List<Article> articles = articleRepository.findAllByCategory(toDelete);
        for (Article a : articles) {
            a.setCategory(defaultCategory);
        }
        articleRepository.saveAll(articles);

        try {
            categoryRepository.delete(toDelete);
        } catch (DataIntegrityViolationException e) {
            throw new DataBaseException("Falha de integridade referencial");
        }
    }

    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        return toDTO(c);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> listAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private CategoryDTO toDTO(Category entity) {
        return new CategoryDTO(entity.getId(), entity.getName());
    }
}