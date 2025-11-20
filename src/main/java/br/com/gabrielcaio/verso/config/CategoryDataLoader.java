package br.com.gabrielcaio.verso.config;

import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class CategoryDataLoader implements CommandLineRunner
{

    private final CategoryRepository categoryRepository;

    public CategoryDataLoader(CategoryRepository categoryRepository)
    {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args)
    {
        String defaultName = "Sem categoria";
        categoryRepository.findByName(defaultName)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(defaultName)
                        .build()));
    }
}
