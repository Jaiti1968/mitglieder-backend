package de.emc.mitglieder.repository.user;

import de.emc.mitglieder.dto.admin.UserAdminResponse;
import de.emc.mitglieder.security.AppUserDetails;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    public List<UserAdminResponse> findAllForAdmin() {
        String sql = """
                SELECT
                    id,
                    username,
                    role,
                    active,
                    created_at,
                    last_login_at
                FROM tblUsers
                ORDER BY username
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new UserAdminResponse(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("role"),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("last_login_at") != null
                        ? rs.getTimestamp("last_login_at").toLocalDateTime()
                        : null
        ));
    }

    public boolean existsByUsername(String username) {
        String sql = """
                SELECT COUNT(*)
                FROM tblUsers
                WHERE username = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);

        return count != null && count > 0;
    }

    public void createUser(String username, String passwordHash, String role) {
        String sql = """
                INSERT INTO tblUsers (
                    username,
                    password_hash,
                    role,
                    active
                )
                VALUES (?, ?, ?, TRUE)
                """;

        jdbcTemplate.update(sql, username, passwordHash, role);
    }

    public boolean existsById(Long id) {
        String sql = """
                SELECT COUNT(*)
                FROM tblUsers
                WHERE id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);

        return count != null && count > 0;
    }

    public boolean isActiveAdmin(Long id) {
        String sql = """
            SELECT COUNT(*)
            FROM tblUsers
            WHERE id = ?
              AND role = 'ADMIN'
              AND active = TRUE
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);

        return count != null && count > 0;
    }

    public int countActiveAdmins() {
        String sql = """
            SELECT COUNT(*)
            FROM tblUsers
            WHERE role = 'ADMIN'
              AND active = TRUE
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

        return count != null ? count : 0;
    }

    public void updateRole(Long id, String role) {
        String sql = """
                UPDATE tblUsers
                SET role = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, role, id);
    }

    public void updateActive(Long id, boolean active) {
        String sql = """
                UPDATE tblUsers
                SET active = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, active, id);
    }

    public void updatePassword(Long id, String passwordHash) {
        String sql = """
                UPDATE tblUsers
                SET password_hash = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, passwordHash, id);
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