package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.config.BaseIntegrationTest;
import br.com.gabrielcaio.verso.dtos.ArticleResponseWithTitleAndStatusAndCategoryName;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import br.com.gabrielcaio.verso.dtos.UpdateArticleRequestDTO;
import br.com.gabrielcaio.verso.services.ArticleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
@ActiveProfiles("test")
class ArticleControllerTest extends BaseIntegrationTest
{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateArticleRequestDTO createArticleRequestDTO;
    private CreateArticleResponseDTO createArticleResponseDTO;
    private ArticleResponseWithTitleAndStatusAndCategoryName articleResponse;
    private UpdateArticleRequestDTO updateArticleRequestDTO;

    @BeforeEach
    void setUp()
    {
        // Configurar DTOs de teste com dados válidos
        createArticleRequestDTO = new CreateArticleRequestDTO(
                "Test Article Title Valid",
                "This is a test content with more than 10 characters for validation",
                "Technology",
                "RASCUNHO"
        );

        createArticleResponseDTO = new CreateArticleResponseDTO(
                "Test Article Title Valid",
                "This is a test content with more than 10 characters for validation",
                "Technology"
        );

        articleResponse = new ArticleResponseWithTitleAndStatusAndCategoryName(
                "Test Article Title Valid",
                "This is a test content with more than 10 characters for validation",
                "Technology"
        );

        updateArticleRequestDTO = new UpdateArticleRequestDTO(
                "Updated Article Title Valid",
                "Updated content with more than 10 characters for validation",
                "Science",
                "PUBLICADO"
        );
    }

    @Test
    @WithMockUser
    void create_ShouldReturnCreatedArticle() throws Exception
    {
        when(articleService.create(any(CreateArticleRequestDTO.class)))
                .thenReturn(createArticleResponseDTO);

        mockMvc.perform(post("/verso/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createArticleRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Article Title Valid"))
                .andExpect(jsonPath("$.content").value(
                        "This is a test content with more than 10 characters for validation"))
                .andExpect(jsonPath("$.category").value("Technology"));

        verify(articleService, times(1)).create(any(CreateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void create_ShouldReturnUnprocessableEntity_WhenTitleIsBlank() throws Exception
    {
        CreateArticleRequestDTO invalidRequest = new CreateArticleRequestDTO(
                "", // título vazio
                "Valid content with more than 10 characters",
                "Technology",
                "RASCUNHO"
        );

        mockMvc.perform(post("/verso/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.errors[0].fieldName").value("title"))
                .andExpect(jsonPath(
                        "$.errors[0].message").exists()); // Apenas verifica que há uma mensagem para o campo title

        verify(articleService, never()).create(any(CreateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void create_ShouldReturnUnprocessableEntity_WhenContentIsTooShort() throws Exception
    {
        CreateArticleRequestDTO invalidRequest = new CreateArticleRequestDTO(
                "Valid Title",
                "Short", // conteúdo muito curto
                "Technology",
                "RASCUNHO"
        );

        mockMvc.perform(post("/verso/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.errors[0].fieldName").value("content"))
                .andExpect(jsonPath("$.errors[0].message").value(
                        "Conteúdo deve ter pelo menos 10 caracteres"));

        verify(articleService, never()).create(any(CreateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void create_ShouldReturnUnprocessableEntity_WhenCategoryIsBlank() throws Exception
    {
        CreateArticleRequestDTO invalidRequest = new CreateArticleRequestDTO(
                "Valid Title",
                "Valid content with more than 10 characters",
                "", // categoria vazia
                "RASCUNHO"
        );

        mockMvc.perform(post("/verso/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.errors[0].fieldName").value("category"))
                .andExpect(jsonPath("$.errors[0].message").value("Categoria é obrigatória"));

        verify(articleService, never()).create(any(CreateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void create_ShouldReturnUnprocessableEntity_WhenStatusIsBlank() throws Exception
    {
        CreateArticleRequestDTO invalidRequest = new CreateArticleRequestDTO(
                "Valid Title",
                "Valid content with more than 10 characters",
                "Technology",
                "" // status vazio
        );

        mockMvc.perform(post("/verso/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.errors[0].fieldName").value("status"))
                .andExpect(jsonPath("$.errors[0].message").value("Status é obrigatório"));

        verify(articleService, never()).create(any(CreateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void findAllPublished_ShouldReturnPageOfArticles() throws Exception
    {
        Page<ArticleResponseWithTitleAndStatusAndCategoryName> page = new PageImpl<>(
                List.of(articleResponse),
                PageRequest.of(0, 10),
                1
        );

        when(articleService.findAllArticlesPublicados(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/verso/articles")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Article Title Valid"))
                .andExpect(jsonPath("$.content[0].content").value(
                        "This is a test content with more than 10 characters for validation"))
                .andExpect(jsonPath("$.content[0].category").value("Technology"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10));

        verify(articleService, times(1)).findAllArticlesPublicados(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void findAllDrafts_ShouldReturnPageOfDrafts() throws Exception
    {
        Page<ArticleResponseWithTitleAndStatusAndCategoryName> page = new PageImpl<>(
                List.of(articleResponse),
                PageRequest.of(0, 10),
                1
        );

        when(articleService.findAllArticlesRascunho(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/verso/articles/drafts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Article Title Valid"))
                .andExpect(jsonPath("$.content[0].category").value("Technology"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(articleService, times(1)).findAllArticlesRascunho(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void findById_ShouldReturnArticle() throws Exception
    {
        Long articleId = 1L;
        when(articleService.findById(articleId)).thenReturn(articleResponse);

        mockMvc.perform(get("/verso/articles/{id}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article Title Valid"))
                .andExpect(jsonPath("$.content").value(
                        "This is a test content with more than 10 characters for validation"))
                .andExpect(jsonPath("$.category").value("Technology"));

        verify(articleService, times(1)).findById(articleId);
    }

    @Test
    @WithMockUser
    void update_ShouldReturnUpdatedArticle() throws Exception
    {
        Long articleId = 1L;
        when(articleService.update(eq(articleId), any(UpdateArticleRequestDTO.class)))
                .thenReturn(articleResponse);

        mockMvc.perform(put("/verso/articles/{id}", articleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateArticleRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article Title Valid"))
                .andExpect(jsonPath("$.content").value(
                        "This is a test content with more than 10 characters for validation"))
                .andExpect(jsonPath("$.category").value("Technology"));

        verify(articleService, times(1)).update(eq(articleId), any(UpdateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void update_ShouldReturnUnprocessableEntity_WhenContentIsTooShort() throws Exception
    {
        Long articleId = 1L;
        UpdateArticleRequestDTO invalidRequest = new UpdateArticleRequestDTO(
                "Valid Title",
                "Short", // conteúdo muito curto
                "Technology",
                "PUBLICADO"
        );

        mockMvc.perform(put("/verso/articles/{id}", articleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.errors[0].fieldName").value("content"))
                .andExpect(jsonPath("$.errors[0].message").value(
                        "Conteúdo deve ter pelo menos 10 caracteres"));

        verify(articleService, never()).update(anyLong(), any(UpdateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void update_ShouldReturnUnprocessableEntity_WhenTitleIsTooShort() throws Exception
    {
        Long articleId = 1L;
        UpdateArticleRequestDTO invalidRequest = new UpdateArticleRequestDTO(
                "AB", // título muito curto (min 3)
                "Valid content with more than 10 characters",
                "Technology",
                "PUBLICADO"
        );

        mockMvc.perform(put("/verso/articles/{id}", articleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.errors[0].fieldName").value("title"))
                .andExpect(jsonPath("$.errors[0].message").value(
                        "Título deve ter entre 3 e 200 caracteres"));

        verify(articleService, never()).update(anyLong(), any(UpdateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void delete_ShouldReturnNoContent() throws Exception
    {
        Long articleId = 1L;
        doNothing().when(articleService)
                .delete(articleId);

        mockMvc.perform(delete("/verso/articles/{id}", articleId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(articleService, times(1)).delete(articleId);
    }

    @Test
    void create_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception
    {
        mockMvc.perform(post("/verso/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createArticleRequestDTO)))
                .andExpect(status().isUnauthorized());

        verify(articleService, never()).create(any(CreateArticleRequestDTO.class));
    }

    @Test
    void findAllPublished_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception
    {
        mockMvc.perform(get("/verso/articles"))
                .andExpect(status().isUnauthorized());

        verify(articleService, never()).findAllArticlesPublicados(any(Pageable.class));
    }

    @Test
    void findAllDrafts_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception
    {
        mockMvc.perform(get("/verso/articles/drafts"))
                .andExpect(status().isUnauthorized());

        verify(articleService, never()).findAllArticlesRascunho(any(Pageable.class));
    }

    @Test
    void findById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception
    {
        mockMvc.perform(get("/verso/articles/1"))
                .andExpect(status().isUnauthorized());

        verify(articleService, never()).findById(anyLong());
    }

    @Test
    void update_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception
    {
        mockMvc.perform(put("/verso/articles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateArticleRequestDTO)))
                .andExpect(status().isUnauthorized());

        verify(articleService, never()).update(anyLong(), any(UpdateArticleRequestDTO.class));
    }

    @Test
    void delete_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception
    {
        mockMvc.perform(delete("/verso/articles/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(articleService, never()).delete(anyLong());
    }

    @Test
    @WithMockUser
    void create_ShouldValidateMinimumValidData() throws Exception
    {
        // Teste com dados mínimos válidos
        CreateArticleRequestDTO minimalValidRequest = new CreateArticleRequestDTO(
                "ABC", // mínimo 3 caracteres
                "Exactly10c", // exatamente 10 caracteres (mínimo)
                "Cat",
                "RASCUNHO"
        );

        CreateArticleResponseDTO minimalResponse = new CreateArticleResponseDTO(
                "ABC",
                "Exactly10c",
                "Cat"
        );

        when(articleService.create(any(CreateArticleRequestDTO.class)))
                .thenReturn(minimalResponse);

        mockMvc.perform(post("/verso/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalValidRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("ABC"))
                .andExpect(jsonPath("$.content").value("Exactly10c"))
                .andExpect(jsonPath("$.category").value("Cat"));

        verify(articleService, times(1)).create(any(CreateArticleRequestDTO.class));
    }

    @Test
    @WithMockUser
    void create_ShouldReturnUnprocessableEntity_WhenMultipleValidationsFail() throws Exception
    {
        // Teste quando múltiplas validações falham - título vazio
        CreateArticleRequestDTO invalidRequest = new CreateArticleRequestDTO(
                "", // título vazio - viola @NotBlank e @Size
                "Short", // conteúdo curto - viola @Size (e possivelmente @NotBlank)
                "", // categoria vazia - viola @NotBlank
                "" // status vazio - viola @NotBlank
        );

        mockMvc.perform(post("/verso/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(greaterThanOrEqualTo(
                        4))); // 5 erros: título (2), conteúdo (1), categoria (1), status (1)

        verify(articleService, never()).create(any(CreateArticleRequestDTO.class));
    }
}