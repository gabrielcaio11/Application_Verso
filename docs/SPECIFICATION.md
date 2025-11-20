# Verso

## 1. Visão Geral

O Verso é uma plataforma voltada à criação, publicação e gerenciamento de artigos por usuários.  
Seu principal objetivo é permitir o compartilhamento de conhecimento de forma estruturada e
organizada.

---

## 2. Perfis de Usuário

### 2.1 Usuário Comum

- Pode se cadastrar e autenticar no sistema.
- Pode criar, editar e excluir seus próprios artigos.
- Pode salvar artigos como rascunho ou publicar.
- Pode visualizar artigos publicados por outros usuários.
- Pode interagir com artigos (curtir, comentar, favoritar)

### 2.2 Administrador

- Possui todas as permissões de um usuário comum.
- Pode gerenciar usuários, artigos e categorias.
- Pode publicar ou despublicar artigos.
- Pode promover ou rebaixar usuários de papel (USER ↔ ADMIN).
- Pode visualizar métricas e relatórios.

---

## 3. Regras de Negócio (RN)

| Código   | Descrição                                                                                     |
|----------|-----------------------------------------------------------------------------------------------|
| **RN01** | Cada usuário deve possuir um email único.                                                     |
| **RN02** | A senha do usuário deve ser armazenada de forma criptografada.                                |
| **RN03** | Um artigo pertence a apenas um autor.                                                         |
| **RN04** | Um artigo deve possuir um status: `RASCUNHO` ou `PUBLICADO`.                                  |
| **RN05** | Artigos em rascunho só podem ser visualizados pelo autor.                                     |
| **RN06** | Apenas o autor pode editar ou excluir seus próprios artigos.                                  |
| **RN07** | Categorias devem ter nomes únicos.                                                            |
| **RN08** | Somente administradores podem criar, editar ou excluir categorias.                            |
| **RN09** | Somente administradores podem alterar o papel de um usuário.                                  |
| **RN10** | Artigos excluídos pelo autor ficam com status inativo.                                        |
| **RN11** | Cada artigo pode pertencer a uma única categoria.                                             |
| **RN12** | A exclusão de uma categoria define a categoria do artigo como “Sem categoria”.                |
| **RN13** | Apenas usuários autenticados podem criar ou editar artigos.                                   |
| **RN14** | Comentários e curtidas só podem ser feitos por usuários autenticados (funcionalidade futura). |
| **RN15** | Um artigo publicado é visível para qualquer visitante.                                        |

## 4. Extensões Futuras

- **Upload de Imagens:** inclusão de mídia nos artigos.
- **Sistema de Comentários:** permitir discussões nos artigos.
- **Favoritos:** salvar artigos para leitura posterior.
- **Notificações:** alertar seguidores sobre novos artigos publicados.
- **Recomendações:** exibir artigos semelhantes com base em categorias ou autor.

---

