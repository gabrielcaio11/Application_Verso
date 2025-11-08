# Verso

## 📋 Sobre o Projeto

O **Verso** é uma plataforma de publicação e gerenciamento de artigos desenvolvida em Java com Spring Boot. A aplicação permite que usuários criem, publiquem e gerenciem artigos de forma estruturada, com controle de acesso baseado em perfis de usuário (comum e administrador).

A plataforma foi projetada para facilitar o compartilhamento de conhecimento, oferecendo recursos como categorização de artigos, sistema de rascunhos, controle de publicação, gerenciamento de usuários, sistema de interações (comentários, reações e favoritos) e notificações.

## 🚀 Funcionalidades

### Funcionalidades Principais

- **Autenticação e Autorização**
  - Sistema de autenticação HTTP Basic
  - Controle de acesso baseado em roles
  - Registro de novos usuários
  - Criptografia de senhas com BCrypt

- **Gerenciamento de Artigos**
  - Criação, edição e exclusão de artigos
  - Sistema de status: `RASCUNHO` e `PUBLICADO`
  - Artigos em rascunho visíveis apenas para o autor
  - Artigos publicados visíveis para todos os usuários autenticados
  - Associação de artigos a categorias
  - Paginação e ordenação de resultados
  - Busca de artigos publicados
  - Busca de rascunhos do usuário autenticado

- **Gerenciamento de Categorias** (apenas ADMIN)
  - Criação, edição e exclusão de categorias
  - Listagem de categorias com paginação
  - Validação de nomes únicos
  - Migração automática de artigos ao excluir categoria

- **Gerenciamento de Usuários**
  - Registro de novos usuários
  - Listagem de usuários (apenas ADMIN)
  - Sistema de roles (USER e ADMIN)
  - Validação de email único

- **Sistema de Favoritos**
  - Adicionar artigos publicados aos favoritos
  - Remover artigos dos favoritos
  - Listar artigos favoritados com paginação
  - Verificar se um artigo está nos favoritos
  - Cada usuário pode favoritar apenas artigos publicados

- **Sistema de Seguidores**
  - Seguir e deixar de seguir outros usuários
  - Listar usuários que você está seguindo
  - Listar seus seguidores
  - Visualizar perfil de usuário com contagem de seguidores
  - Verificar se está seguindo um usuário
  - Não é possível seguir a si mesmo

- **Sistema de Notificações**
  - Receber notificações quando autores seguidos publicam novos artigos
  - Listar todas as notificações com paginação
  - Listar apenas notificações não lidas
  - Marcar notificações como lidas
  - Marcar todas as notificações como lidas
  - Contar notificações não lidas

- **Documentação de API**
  - Interface Swagger UI para testes e documentação
  - Documentação OpenAPI 3.0 completa
  - Exemplos de requisições e respostas

- **Sistema de Comentários**
  - Comentar em artigos publicados (comentário raiz ou resposta usando `parentId`)
  - Listagem paginada
    - Threaded (raiz paginada com respostas aninhadas)
  - Criação de comentários
  - Exclusão de comentário
    - Permissões: autor do comentário ou autor do artigo
  - Contador de comentários por artigo (`comments_count`) mantido em criação/remoção
  - Notificações:
    - Autor do artigo é notificado ao receber novo comentário
    - Autor do comentário é notificado ao receber uma resposta

- **Sistema de Curtidas e Reações**
  - Reagir a artigos publicados com diferentes tipos de reação (LIKE, LOVE, LAUGH, WOW, SAD, ANGRY)
  - Adicionar ou atualizar reação em um artigo
  - Remover reação de um artigo
  - Listar todas as reações de um artigo com paginação
  - Listar todas as reações do usuário autenticado
  - Obter estatísticas detalhadas de reações por artigo (contagem por tipo)
  - Verificar qual reação o usuário autenticado deu em um artigo
  - Contador de curtidas (`likes_count`) mantido automaticamente
  - Apenas artigos publicados podem receber reações
  - Cada usuário pode ter apenas uma reação por artigo (atualizável)

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.6** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM
- **Spring Security** - Segurança e autenticação
- **Spring Web** - API REST
- **Spring Validation** - Validação de dados

### Banco de Dados
- **PostgreSQL 18** - Banco de dados relacional

### Ferramentas e Bibliotecas
- **Lombok** - Redução de boilerplate
- **MapStruct** - Mapeamento de objetos
- **Springdoc OpenAPI** - Documentação da API
- **Dotenv Java** - Gerenciamento de variáveis de ambiente
- **BCrypt** - Criptografia de senhas

### Testes
- **JUnit 5** - Framework de testes
- **Mockito** - Mocking para testes

### DevOps
- **Docker** - Containerização
- **Docker Compose** - Orquestração de containers
- **Maven** - Gerenciamento de dependências

## 📦 Pré-requisitos

- `Docker` instalado na máquina

### Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:
```
POSTGRES_DB=name_db
POSTGRES_USER=user_db
POSTGRES_PASSWORD=password_db
SERVER_PORT=8080
```
## 🚀 Como Executar

### Usando Docker Compose

1. Clone o repositório:
    ```
    git clone https://github.com/gabrielcaio11/Application_Verso.git
    ```
    ```
    cd Application_Verso
    ```
2. Crie o arquivo `.env` com as variáveis de ambiente necessárias (veja seção Pré-requisitos)

3. Execute o Docker Compose para montar as imagens e subir os containers:
    ```
    docker compose up --build
    ```
4. Aguarde a aplicação iniciar. Você verá mensagens indicando que a aplicação está rodando.

5. Acesse a aplicação:
   - **API Base**: `http://localhost:8080`
   - **Swagger UI**: `http://localhost:8080/swagger-ui.html`
   - **OpenAPI Docs**: `http://localhost:8080/api-docs`

### Banco de Dados

O PostgreSQL estará disponível na porta `5433` (configurável no `docker-compose.yaml`).

## 📚 Documentação da API

A documentação completa da API está disponível através do Swagger UI em:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

A documentação inclui:
- Todos os endpoints disponíveis
- Parâmetros de requisição e resposta
- Exemplos de uso
- Códigos de status HTTP
- Possibilidade de testar os endpoints diretamente pela interface

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas:

- **Controllers** - Camada de apresentação (REST API)
- **Services** - Lógica de negócio
- **Repositories** - Acesso a dados
- **DTOs** - Objetos de transferência de dados
- **Mappers** - Conversão entre entidades e DTOs (MapStruct)
- **Validators** - Validações de negócio
- **Domain** - Entidades e enums do domínio
- **Security** - Configurações de segurança
- **Config** - Configurações da aplicação

## 🔐 Segurança

- Autenticação HTTP Basic
- Senhas criptografadas com BCrypt (strength 10)
- Controle de acesso baseado em roles
- Endpoints protegidos por autenticação
- Validação de dados de entrada

## 📝 Regras de Negócio Principais

- Cada usuário possui um email único
- Artigos em rascunho só podem ser visualizados pelo autor
- Apenas o autor pode editar ou excluir seus próprios artigos
- Apenas administradores podem gerenciar categorias
- Categorias têm nomes únicos
- Ao excluir uma categoria, os artigos são movidos para a categoria padrão
- Apenas artigos publicados podem receber reações e comentários
- Cada usuário pode ter apenas uma reação por artigo (atualizável)

## 🔮 Implementações Futuras

- **Upload de Imagens** - Inclusão de mídia nos artigos
- **Recomendações** - Exibir artigos semelhantes com base em categorias ou autor
- **Busca Avançada** - Filtros por categoria, autor, data, palavras-chave
- **Estatísticas e Métricas** - Visualização de dados sobre artigos e usuários
- **Exportação de Artigos** - Exportar artigos em diferentes formatos (PDF, Markdown)
- **Editor Rich Text** - Editor WYSIWYG para criação de artigos
- **Versionamento de Artigos** - Histórico de alterações nos artigos
- **Tags** - Sistema de tags além de categorias
- **Autenticação JWT** - Substituir HTTP Basic por tokens JWT
- **API Rate Limiting** - Limitação de requisições por usuário

## 📫 Contato

- **LinkedIn**: [Gabriel Caio](https://www.linkedin.com/in/gabriel-caio/)
* **Email:** [Gabriel Caio](mailto:gabri3lcaiodev@gmail.com)
---