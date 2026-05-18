package de.emc.mitglieder.repository.user;

import de.emc.mitglieder.security.AppUserDetails;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AppUserDetails> findByUsername(String username) {
        String sql = """
                SELECT
                    id,
                    username,
                    password_hash,
                    role,
                    active
                FROM tblUsers
                WHERE username = ?
                """;

        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }

            AppUserDetails user = new AppUserDetails(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role"),
                    rs.getBoolean("active")
            );

            return Optional.of(user);
        }, username);
    }

    public void updateLastLogin(Long userId) {
        String sql = """
                UPDATE tblUsers
                SET last_login_at = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, LocalDateTime.now(), userId);
    }
}