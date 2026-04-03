package com.crackview.repository;

import com.crackview.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .targetCompany("Google")
                .targetRole("Backend Engineer")
                .build();
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("save - should persist user with auto-generated id and uuid")
    void save_shouldPersistUser() {
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUuid()).isNotNull().hasSize(36);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByUuid - should return user when uuid exists")
    void findByUuid_shouldReturnUser() {
        Optional<User> found = userRepository.findByUuid(savedUser.getUuid());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("findByUuid - should return empty when uuid not found")
    void findByUuid_shouldReturnEmpty_whenNotFound() {
        Optional<User> found = userRepository.findByUuid("non-existent-uuid");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByUsername - should return user when username exists")
    void findByUsername_shouldReturnUser() {
        Optional<User> found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("findByUsername - should return empty when username not found")
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByEmail - should return user when email exists")
    void findByEmail_shouldReturnUser() {
        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("existsByUsername - should return true when username exists")
    void existsByUsername_shouldReturnTrue() {
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
    }

    @Test
    @DisplayName("existsByUsername - should return false when username not found")
    void existsByUsername_shouldReturnFalse_whenNotFound() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    @DisplayName("existsByEmail - should return true when email exists")
    void existsByEmail_shouldReturnTrue() {
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - should return false when email not found")
    void existsByEmail_shouldReturnFalse_whenNotFound() {
        assertThat(userRepository.existsByEmail("nope@example.com")).isFalse();
    }
}
