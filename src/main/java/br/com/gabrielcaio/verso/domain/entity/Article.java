package br.com.gabrielcaio.verso.domain.entity;

import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_articles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PUBLICADO'")
    private ArticleStatus status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_article_user"))
    private User author;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_article_category"))
    private Category category;

    @Column(name = "comments_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    @Builder.Default
    private Long commentsCount = 0L;

    @Column(name = "likes_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    @Builder.Default
    private Long likesCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}