package com.college.cashflow.service;

import com.college.cashflow.dto.DeptAdminLoginRequest;
import com.college.cashflow.dto.LoginResponse;
import com.college.cashflow.dto.MainAdminLoginRequest;
import com.college.cashflow.entity.Department;
import com.college.cashflow.entity.User;
import com.college.cashflow.enums.Role;
import com.college.cashflow.repository.UserRepository;
import com.college.cashflow.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthService — stub-based, no Mockito (avoids JDK 21 byte-buddy agent hang).
 */
class AuthServiceTest {

    // ── Fixtures ─────────────────────────────────────────────────────────────────
    private User mainAdmin;
    private User deptAdmin;
    private Department cseDept;

    @BeforeEach
    void setUp() {
        mainAdmin = User.builder()
                .id(1L).username("admin").passwordHash("hashedPass")
                .role(Role.MAIN_ADMIN).fullName("Main Admin").active(true).build();

        cseDept = Department.builder()
                .id(10L).name("Computer Science").code("CSE").active(true).build();

        deptAdmin = User.builder()
                .id(2L).username("cseadmin").passwordHash("hashedPass")
                .role(Role.DEPT_ADMIN).fullName("CSE Admin")
                .department(cseDept).parentAdmin(mainAdmin).active(true).build();
    }

    // ── Stub builders ─────────────────────────────────────────────────────────────

    /** UserRepository stub backed by a simple map. */
    private UserRepository userRepoStub(Map<String, User> byUsername) {
        return new UserRepository() {
            @Override public Optional<User> findByUsername(String u) { return Optional.ofNullable(byUsername.get(u)); }
            @Override public Optional<User> findByUsernameAndRole(String u, Role r) {
                User found = byUsername.get(u);
                return (found != null && found.getRole() == r) ? Optional.of(found) : Optional.empty();
            }
            @Override public List<User> findByRole(Role r) { return Collections.emptyList(); }
            @Override public boolean existsByUsername(String u) { return byUsername.containsKey(u); }
            @Override public boolean existsByUsernameAndIdNot(String u, Long id) { return false; }
            @Override public boolean existsByEmail(String e) { return false; }
            @Override public boolean existsByEmailAndIdNot(String e, Long id) { return false; }
            // JpaRepository boilerplate — unused by these tests
            @Override public List<User> findAll() { return new ArrayList<>(byUsername.values()); }
            @Override public List<User> findAll(Sort s) { return findAll(); }
            @Override public Page<User> findAll(Pageable p) { return Page.empty(); }
            @Override public List<User> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends User> S save(S e) { return e; }
            @Override public <S extends User> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public Optional<User> findById(Long id) { return Optional.empty(); }
            @Override public boolean existsById(Long id) { return false; }
            @Override public long count() { return byUsername.size(); }
            @Override public void deleteById(Long id) {}
            @Override public void delete(User e) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends User> es) {}
            @Override public void deleteAll() {}
            @Override public <S extends User> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
            @Override public <S extends User> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
            @Override public <S extends User> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
            @Override public <S extends User> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
            @Override public <S extends User> long count(Example<S> ex) { return 0; }
            @Override public <S extends User> boolean exists(Example<S> ex) { return false; }
            @Override public <S extends User, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { return null; }
            @Override public void flush() {}
            @Override public <S extends User> S saveAndFlush(S e) { return e; }
            @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> es) { return Collections.emptyList(); }
            @Override public void deleteAllInBatch(Iterable<User> es) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public User getOne(Long id) { return null; }
            @Override public User getById(Long id) { return null; }
            @Override public User getReferenceById(Long id) { return null; }
        };
    }

    /** PasswordEncoder stub: matches if raw.equals(encoded.replace("hashed","raw")) */
    private PasswordEncoder passwordEncoderStub(boolean result) {
        return new PasswordEncoder() {
            @Override public String encode(CharSequence r) { return "hashed" + r; }
            @Override public boolean matches(CharSequence r, String e) { return result; }
        };
    }

    /** JwtTokenProvider stub: always returns a fixed token string. */
    private JwtTokenProvider jwtStub(String token) {
        return new JwtTokenProvider("test-secret-for-jwt-that-is-32chars!", 3_600_000L) {
            @Override public String generateToken(User u) { return token; }
        };
    }

    private AuthService buildService(UserRepository repo, PasswordEncoder pe, JwtTokenProvider jwt) {
        return new AuthService(repo, pe, jwt);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────────

    @Test
    void mainAdminLogin_success_returnsToken() {
        AuthService svc = buildService(
                userRepoStub(Map.of("admin", mainAdmin)),
                passwordEncoderStub(true),
                jwtStub("mock.jwt.token")
        );

        LoginResponse resp = svc.mainAdminLogin(new MainAdminLoginRequest("admin", "rawPass"));

        assertNotNull(resp);
        assertEquals("mock.jwt.token", resp.getToken());
        assertEquals("admin", resp.getUsername());
        assertEquals("MAIN_ADMIN", resp.getRole());
    }

    @Test
    void mainAdminLogin_wrongPassword_throwsBadCredentials() {
        AuthService svc = buildService(
                userRepoStub(Map.of("admin", mainAdmin)),
                passwordEncoderStub(false),
                jwtStub("token")
        );
        assertThrows(BadCredentialsException.class,
                () -> svc.mainAdminLogin(new MainAdminLoginRequest("admin", "wrong")));
    }

    @Test
    void mainAdminLogin_deactivatedAccount_throwsBadCredentials() {
        mainAdmin.setActive(false);
        AuthService svc = buildService(
                userRepoStub(Map.of("admin", mainAdmin)),
                passwordEncoderStub(true),
                jwtStub("token")
        );
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> svc.mainAdminLogin(new MainAdminLoginRequest("admin", "rawPass")));
        assertEquals("Account is deactivated", ex.getMessage());
    }

    @Test
    void mainAdminLogin_nonMainAdminRole_throwsBadCredentials() {
        AuthService svc = buildService(
                userRepoStub(Map.of("cseadmin", deptAdmin)),
                passwordEncoderStub(true),
                jwtStub("token")
        );
        assertThrows(BadCredentialsException.class,
                () -> svc.mainAdminLogin(new MainAdminLoginRequest("cseadmin", "rawPass")));
    }

    @Test
    void deptAdminLogin_success_returnsTokenWithDeptInfo() {
        AuthService svc = buildService(
                userRepoStub(Map.of("admin", mainAdmin, "cseadmin", deptAdmin)),
                passwordEncoderStub(true),
                jwtStub("mock.dept.token")
        );

        LoginResponse resp = svc.deptAdminLogin(new DeptAdminLoginRequest("admin", "cseadmin", "rawPass"));

        assertNotNull(resp);
        assertEquals("mock.dept.token", resp.getToken());
        assertEquals("cseadmin", resp.getUsername());
        assertEquals(10L, resp.getDepartmentId());
        assertEquals("Computer Science", resp.getDepartmentName());
    }

    @Test
    void deptAdminLogin_deactivatedMainAdmin_throwsBadCredentials() {
        mainAdmin.setActive(false);
        AuthService svc = buildService(
                userRepoStub(Map.of("admin", mainAdmin, "cseadmin", deptAdmin)),
                passwordEncoderStub(true),
                jwtStub("token")
        );
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> svc.deptAdminLogin(new DeptAdminLoginRequest("admin", "cseadmin", "rawPass")));
        assertEquals("Admin account is deactivated", ex.getMessage());
    }

    @Test
    void deptAdminLogin_deactivatedDeptAdmin_throwsBadCredentials() {
        deptAdmin.setActive(false);
        AuthService svc = buildService(
                userRepoStub(Map.of("admin", mainAdmin, "cseadmin", deptAdmin)),
                passwordEncoderStub(true),
                jwtStub("token")
        );
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> svc.deptAdminLogin(new DeptAdminLoginRequest("admin", "cseadmin", "rawPass")));
        assertEquals("Department admin account is deactivated", ex.getMessage());
    }

    @Test
    void deptAdminLogin_wrongParentAdmin_throwsBadCredentials() {
        User otherAdmin = User.builder().id(99L).username("other").role(Role.MAIN_ADMIN).active(true).build();
        deptAdmin.setParentAdmin(otherAdmin);
        AuthService svc = buildService(
                userRepoStub(Map.of("admin", mainAdmin, "cseadmin", deptAdmin)),
                passwordEncoderStub(true),
                jwtStub("token")
        );
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> svc.deptAdminLogin(new DeptAdminLoginRequest("admin", "cseadmin", "rawPass")));
        assertEquals("Invalid admin hierarchy", ex.getMessage());
    }

    @Test
    void deptAdminLogin_unassignedDepartment_throwsBadCredentials() {
        deptAdmin.setDepartment(null);
        AuthService svc = buildService(
                userRepoStub(Map.of("admin", mainAdmin, "cseadmin", deptAdmin)),
                passwordEncoderStub(true),
                jwtStub("token")
        );
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> svc.deptAdminLogin(new DeptAdminLoginRequest("admin", "cseadmin", "rawPass")));
        assertEquals("No department assigned to this admin", ex.getMessage());
    }
}
