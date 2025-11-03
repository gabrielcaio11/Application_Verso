package br.com.gabrielcaio.verso.mappers;

import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.dtos.ArticleResponseWithTitleAndStatusAndCategoryName;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = ArticleStatus.class)
public interface ArticleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "status", expression = "java(ArticleStatus.PUBLICADO)")
    Article toEntity(CreateArticleRequestDTO dto);

    @Mapping(source = "category.name", target = "category")
    CreateArticleResponseDTO toCreateResponse(Article entity);

    @Mapping(source = "category.name", target = "category")
    ArticleResponseWithTitleAndStatusAndCategoryName toResponseWithTitleAndStatusAndCategoryName(Article article);
}