package br.com.gabrielcaio.verso.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(
    name = "tb_roles",
    uniqueConstraints = {@UniqueConstraint(name = "uk_role_name", columnNames = "name")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Roles implements GrantedAuthority {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private Long id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Override
  public String getAuthority() {
    return name;
  }

  @Override
  public String toString() {
    return "{\"identifier\":\"" + id + "\", \"name\":\"" + name + "\"}";
  }
}
