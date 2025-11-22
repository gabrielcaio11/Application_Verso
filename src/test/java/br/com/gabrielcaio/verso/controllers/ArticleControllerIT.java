package br.com.gabrielcaio.verso.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.gabrielcaio.verso.config.BaseIT;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.dtos.CreateArticleRequestDTO;
import br.com.gabrielcaio.verso.dtos.CreateArticleResponseDTO;
import br.com.gabrielcaio.verso.dtos.UpdateArticleRequestDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import br.com.gabrielcaio.verso.repositories.RolesRepository;
import br.com.gabrielcaio.verso.repositories.UserRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Slf4j
class ArticleControllerIT extends BaseIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private RolesRepository rolesRepository;

  private String baseUrl;

  @BeforeEach
  void setup() {
    baseUrl = "http://localhost:" + port + "/verso/articles";
    log.info("Base URL: {}", baseUrl);
  }

  private TestRestTemplate restTemplateForAdmin() {
    return restTemplate.withBasicAuth("admin_test", "123456");
  }

  private TestRestTemplate restTemplateForUser() {
    return restTemplate.withBasicAuth("user_test", "123456");
  }

  @Test
  void shouldHaveTestDataLoaded() {
    var roleUser = rolesRepository.findByName("USER");
    var roleAdmin = rolesRepository.findByName("ADMIN");
    var admin = userRepository.findByEmail("admin@test.com");
    var user = userRepository.findByEmail("user@test.com");
    var defaultCategory = categoryRepository.findByName("Sem categoria");
    var techCategory = categoryRepository.findByName("Tecnologia");
    var educationCategory = categoryRepository.findByName("Educação");

    assertThat(roleUser.isPresent()).isTrue();
    assertThat(roleAdmin.isPresent()).isTrue();
    assertThat(admin.isPresent()).isTrue();
    assertThat(user.isPresent()).isTrue();
    assertThat(defaultCategory.isPresent()).isTrue();
    assertThat(techCategory.isPresent()).isTrue();
    assertThat(educationCategory.isPresent()).isTrue();
  }

  @Test
  void shouldCreateArticle() {
    CreateArticleRequestDTO dto =
        new CreateArticleRequestDTO(
            "Meu artigo de teste",
            "Conteúdo do artigo com mais de 10 caracteres para passar na validação",
            "Tecnologia",
            "RASCUNHO");

    ResponseEntity<CreateArticleResponseDTO> response =
        restTemplateForUser().postForEntity(baseUrl, dto, CreateArticleResponseDTO.class);

    log.info("Create Article Response: {}", response.getStatusCode());
    if (response.getBody() != null) {
      log.info("Response Body: {}", response.getBody());
    }

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Meu artigo de teste");
    assertThat(response.getBody().getCategory()).isEqualTo("TECNOLOGIA");
  }

  @Test
  void shouldReturnUnauthorizedWithoutAuth() {
    CreateArticleRequestDTO dto =
        new CreateArticleRequestDTO(
            "Teste sem auth", "Conteúdo com mais de 10 caracteres", "Tecnologia", "RASCUNHO");

    ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, dto, String.class);

    log.info("Unauthorized Response: {}", response.getStatusCode());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldListPublishedArticles() {
    // Primeiro criar um artigo publicado
    CreateArticleRequestDTO dto =
        new CreateArticleRequestDTO(
            "Artigo Publicado para Listagem",
            "Conteúdo publicado com mais de 10 caracteres para validação",
            "Tecnologia",
            "PUBLICADO");

    ResponseEntity<CreateArticleResponseDTO> createResponse =
        restTemplateForUser().postForEntity(baseUrl, dto, CreateArticleResponseDTO.class);

    log.info("Create Published Article Response: {}", createResponse.getStatusCode());
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Agora listar os artigos publicados
    ResponseEntity<String> response = restTemplateForUser().getForEntity(baseUrl, String.class);

    log.info("List Published Articles Response: {}", response.getStatusCode());
    if (response.getBody() != null) {
      log.info("Response Body: {}", response.getBody());
    }

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("Artigo Publicado para Listagem");
  }

  @Test
  @Transactional
  void shouldUpdateOwnArticle() {
    // Primeiro criar um artigo
    CreateArticleRequestDTO createDto =
        new CreateArticleRequestDTO(
            "Artigo Original para Update",
            "Conteúdo original com mais de 10 caracteres para validação",
            "Tecnologia",
            "RASCUNHO");

    ResponseEntity<CreateArticleResponseDTO> createResponse =
        restTemplateForUser().postForEntity(baseUrl, createDto, CreateArticleResponseDTO.class);

    log.info("Create Article for Update Response: {}", createResponse.getStatusCode());
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Buscar o artigo criado para obter o ID
    Optional<Article> createdArticle = articleRepository.findByTitle("Artigo Original para Update");
    log.info("Article found for update: {}", createdArticle.isPresent());
    assertThat(createdArticle).isPresent();

    Long articleId = createdArticle.get().getId();
    log.info("Article ID for update: {}", articleId);

    // Atualizar o artigo
    UpdateArticleRequestDTO updateDto =
        new UpdateArticleRequestDTO(
            "Artigo Atualizado",
            "Conteúdo atualizado com mais de 10 caracteres para validação",
            "Tecnologia",
            "PUBLICADO");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<UpdateArticleRequestDTO> requestEntity = new HttpEntity<>(updateDto, headers);

    ResponseEntity<String> response =
        restTemplateForUser()
            .exchange(baseUrl + "/" + articleId, HttpMethod.PUT, requestEntity, String.class);

    log.info("Update Article Response: {}", response.getStatusCode());
    if (response.getBody() != null) {
      log.info("Update Response Body: {}", response.getBody());
    }

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("Artigo Atualizado");
  }

  @Test
  void shouldDeleteArticle() {
    // Primeiro criar um artigo
    CreateArticleRequestDTO createDto =
        new CreateArticleRequestDTO(
            "Artigo para Deletar",
            "Conteúdo para deletar com mais de 10 caracteres para validação",
            "Tecnologia",
            "RASCUNHO");

    ResponseEntity<CreateArticleResponseDTO> createResponse =
        restTemplateForUser().postForEntity(baseUrl, createDto, CreateArticleResponseDTO.class);

    log.info("Create Article for Delete Response: {}", createResponse.getStatusCode());
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Buscar o artigo criado para obter o ID
    Optional<Article> createdArticle = articleRepository.findByTitle("Artigo para Deletar");
    log.info("Article found for delete: {}", createdArticle.isPresent());
    assertThat(createdArticle).isPresent();

    Long articleId = createdArticle.get().getId();
    log.info("Article ID for delete: {}", articleId);

    // Deletar o artigo
    ResponseEntity<Void> response =
        restTemplateForUser()
            .exchange(baseUrl + "/" + articleId, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

    log.info("Delete Article Response: {}", response.getStatusCode());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    // Verificar que o artigo foi deletado
    Optional<Article> deletedArticle = articleRepository.findById(articleId);
    log.info("Article still exists after delete: {}", deletedArticle.isPresent());
    assertThat(deletedArticle).isEmpty();
  }

  @Test
  @Transactional
  void shouldGetArticleById() {
    // Primeiro criar um artigo
    CreateArticleRequestDTO createDto =
        new CreateArticleRequestDTO(
            "Artigo para Buscar por ID",
            "Conteúdo para buscar por ID com mais de 10 caracteres para validação",
            "Tecnologia",
            "PUBLICADO");

    ResponseEntity<CreateArticleResponseDTO> createResponse =
        restTemplateForUser().postForEntity(baseUrl, createDto, CreateArticleResponseDTO.class);

    log.info("Create Article for GetById Response: {}", createResponse.getStatusCode());
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Buscar o artigo criado para obter o ID
    Optional<Article> createdArticle = articleRepository.findByTitle("Artigo para Buscar por ID");
    log.info("Article found for getById: {}", createdArticle.isPresent());
    assertThat(createdArticle).isPresent();

    Long articleId = createdArticle.get().getId();
    log.info("Article ID for getById: {}", articleId);

    // Buscar o artigo por ID
    ResponseEntity<String> response =
        restTemplateForUser().getForEntity(baseUrl + "/" + articleId, String.class);

    log.info("Get Article by ID Response: {}", response.getStatusCode());
    if (response.getBody() != null) {
      log.info("GetById Response Body: {}", response.getBody());
    }

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("Artigo para Buscar por ID");
  }

  @Test
  void shouldListDrafts() {
    // Primeiro criar um rascunho
    CreateArticleRequestDTO dto =
        new CreateArticleRequestDTO(
            "Meu Rascunho",
            "Conteúdo do rascunho com mais de 10 caracteres para validação",
            "Tecnologia",
            "RASCUNHO");

    ResponseEntity<CreateArticleResponseDTO> createResponse =
        restTemplateForUser().postForEntity(baseUrl, dto, CreateArticleResponseDTO.class);

    log.info("Create Draft Response: {}", createResponse.getStatusCode());
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Listar rascunhos
    ResponseEntity<String> response =
        restTemplateForUser().getForEntity(baseUrl + "/drafts", String.class);

    log.info("List Drafts Response: {}", response.getStatusCode());
    if (response.getBody() != null) {
      log.info("List Drafts Response Body: {}", response.getBody());
    }

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("Meu Rascunho");
  }
}
