package com.stride.api.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Todo o SQL da tabela users mora aqui. Nenhuma outra classe escreve query de usuário. */
@Repository
public class UserRepository {

  private static final String FIND_BY_EMAIL =
      """
      SELECT id, name, email, password_hash, created_at
        FROM users
       WHERE lower(email) = lower(:email)
      """;

  private static final String EXISTS_BY_EMAIL =
      "SELECT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower(:email))";

  // RETURNING evita um segundo SELECT: o banco devolve o id e o created_at
  // que ele mesmo gerou, na mesma ida.
  private static final String INSERT =
      """
      INSERT INTO users (name, email, password_hash)
      VALUES (:name, :email, :passwordHash)
      RETURNING id, name, email, password_hash, created_at
      """;

  private final JdbcClient jdbcClient;

  public UserRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public Optional<User> findByEmail(String email) {
    return jdbcClient
        .sql(FIND_BY_EMAIL)
        .param("email", email)
        .query(UserRepository::map)
        .optional();
  }

  public boolean existsByEmail(String email) {
    return jdbcClient.sql(EXISTS_BY_EMAIL).param("email", email).query(Boolean.class).single();
  }

  public User insert(String name, String email, String passwordHash) {
    return jdbcClient
        .sql(INSERT)
        .param("name", name)
        .param("email", email)
        .param("passwordHash", passwordHash)
        .query(UserRepository::map)
        .single();
  }

  private static User map(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
    return new User(
        rs.getObject("id", UUID.class),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("password_hash"),
        rs.getObject("created_at", java.time.OffsetDateTime.class).toInstant());
  }
}
