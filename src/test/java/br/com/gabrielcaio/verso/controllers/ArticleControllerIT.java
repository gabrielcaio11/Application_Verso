package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import br.com.gabrielcaio.verso.dtos.UpdateArticleRequestDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import br.com.gabrielcaio.verso.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ArticleControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/verso/articles";
        // Limpa os artigos antes de cada teste para garantir isolamento
        articleRepository.deleteAll();
    }

    private TestRestTemplate restTemplateForAdmin() {
        return restTemplate.withBasicAuth("admin@test.com", "123456");
    }

    private TestRestTemplate restTemplateForUser() {
        return restTemplate.withBasicAuth("user@test.com", "123456");
    }

    @Test
    void shouldCreateArticle() {
        CreateArticleRequestDTO dto = new CreateArticleRequestDTO(
                "Meu artigo de teste",
                "Conteúdo do artigo com mais de 10 caracteres para passar na validação",
                "Tecnologia",
                "RASCUNHO"
        );

        ResponseEntity<CreateArticleResponseDTO> response = restTemplateForUser().postForEntity(
                baseUrl,
                dto,
                CreateArticleResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Meu artigo de teste");
        assertThat(response.getBody().getCategory()).isEqualTo("Tecnologia");
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuth() {
        CreateArticleRequestDTO dto = new CreateArticleRequestDTO(
                "Teste sem auth",
                "Conteúdo com mais de 10 caracteres",
                "Tecnologia",
                "RASCUNHO"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                dto,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldListPublishedArticles() {
        // Primeiro criar um artigo publicado
        CreateArticleRequestDTO dto = new CreateArticleRequestDTO(
                "Artigo Publicado para Listagem",
                "Conteúdo publicado com mais de 10 caracteres para validação",
                "Tecnologia",
                "PUBLICADO"
        );

        restTemplateForUser().postForEntity(baseUrl, dto, CreateArticleResponseDTO.class);

        // Agora listar os artigos publicados
        ResponseEntity<String> response = restTemplateForUser().getForEntity(
                baseUrl,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Artigo Publicado para Listagem");
    }

    @Test
    void shouldUpdateOwnArticle() {
        // Primeiro criar um artigo
        CreateArticleRequestDTO createDto = new CreateArticleRequestDTO(
                "Artigo Original para Update",
                "Conteúdo original com mais de 10 caracteres para validação",
                "Tecnologia",
                "RASCUNHO"
        );

        ResponseEntity<CreateArticleResponseDTO> createResponse = restTemplateForUser().postForEntity(
                baseUrl,
                createDto,
                CreateArticleResponseDTO.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Buscar o artigo criado para obter o ID
        Optional<Article> createdArticle = articleRepository.findByTitle("Artigo Original para Update");
        assertThat(createdArticle).isPresent();

        Long articleId = createdArticle.get().getId();

        // Atualizar o artigo
        UpdateArticleRequestDTO updateDto = new UpdateArticleRequestDTO(
                "Artigo Atualizado",
                "Conteúdo atualizado com mais de 10 caracteres para validação",
                "Tecnologia",
                "PUBLICADO"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UpdateArticleRequestDTO> requestEntity = new HttpEntity<>(updateDto, headers);

        ResponseEntity<String> response = restTemplateForUser().exchange(
                baseUrl + "/" + articleId,
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Artigo Atualizado");
    }

    @Test
    void shouldDeleteArticle() {
        // Primeiro criar um artigo
        CreateArticleRequestDTO createDto = new CreateArticleRequestDTO(
                "Artigo para Deletar",
                "Conteúdo para deletar com mais de 10 caracteres para validação",
                "Tecnologia",
                "RASCUNHO"
        );

        restTemplateForUser().postForEntity(baseUrl, createDto, CreateArticleResponseDTO.class);

        // Buscar o artigo criado para obter o ID
        Optional<Article> createdArticle = articleRepository.findByTitle("Artigo para Deletar");
        assertThat(createdArticle).isPresent();

        Long articleId = createdArticle.get().getId();

        // Deletar o artigo
        ResponseEntity<Void> response = restTemplateForUser().exchange(
                baseUrl + "/" + articleId,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verificar que o artigo foi deletado
        Optional<Article> deletedArticle = articleRepository.findById(articleId);
        assertThat(deletedArticle).isEmpty();
    }

    @Test
    void shouldGetArticleById() {
        // Primeiro criar um artigo
        CreateArticleRequestDTO createDto = new CreateArticleRequestDTO(
                "Artigo para Buscar por ID",
                "Conteúdo para buscar por ID com mais de 10 caracteres para validação",
                "Tecnologia",
                "PUBLICADO"
        );

        restTemplateForUser().postForEntity(baseUrl, createDto, CreateArticleResponseDTO.class);

        // Buscar o artigo criado para obter o ID
        Optional<Article> createdArticle = articleRepository.findByTitle("Artigo para Buscar por ID");
        assertThat(createdArticle).isPresent();

        Long articleId = createdArticle.get().getId();

        // Buscar o artigo por ID
        ResponseEntity<String> response = restTemplateForUser().getForEntity(
                baseUrl + "/" + articleId,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Artigo para Buscar por ID");
    }

    @Test
    void shouldListDrafts() {
        // Primeiro criar um rascunho
        CreateArticleRequestDTO dto = new CreateArticleRequestDTO(
                "Meu Rascunho",
                "Conteúdo do rascunho com mais de 10 caracteres para validação",
                "Tecnologia",
                "RASCUNHO"
        );

        restTemplateForUser().postForEntity(baseUrl, dto, CreateArticleResponseDTO.class);

        // Listar rascunhos
        ResponseEntity<String> response = restTemplateForUser().getForEntity(
                baseUrl + "/drafts",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Meu Rascunho");
    }
}