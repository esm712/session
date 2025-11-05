package com.hokkom.session.service;

import com.hokkom.session.dto.UserDto;
import com.hokkom.session.entity.User;
import com.hokkom.session.entity.UserRoleType;
import com.hokkom.session.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .roleType(UserRoleType.USER)
                .build();

        userDto = UserDto.builder()
                .username("testuser")
                .password("1234")
                .build();

    }

    @Test
    void register_WhenUserAlreadyExists_ShouldThrowException() {
        // given
        given(userRepository.existsByUsername("testuser")).willReturn(true);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register(userDto));

        assertEquals("이미 존재하는 아이디입니다.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenNewUser_ShouldSaveUserSuccessfully() {
        // given
        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(passwordEncoder.encode("1234")).willReturn("encodedPassword");

        User savedUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .roleType(UserRoleType.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        UserDto result = userService.register(userDto);

        // then
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(any(User.class));
    }

    // 1️⃣ 존재하지 않는 아이디일 경우
    @Test
    void login_WhenUserDoesNotExist_ShouldThrowException() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.login(userDto));

        assertEquals("존재하지 않는 사람입니다.", ex.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // 2️⃣ 비밀번호 불일치
    @Test
    void login_WhenPasswordDoesNotMatch_ShouldThrowException() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(false);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.login(userDto));

        assertEquals("패스워드가 일치하지 않습니다.", ex.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("1234", "encodedPassword");
    }

    // 3️⃣ 로그인 성공
    @Test
    void login_WhenCredentialsAreValid_ShouldReturnUserDto() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);

        // when
        UserDto result = userService.login(userDto);

        // then
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("1234", "encodedPassword");
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }
}