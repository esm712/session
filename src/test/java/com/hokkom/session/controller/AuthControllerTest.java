package com.hokkom.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hokkom.session.dto.UserDto;
import com.hokkom.session.exception.GlobalExceptionHandler;
import com.hokkom.session.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegister_Success() throws Exception {
        // given
        UserDto requestDto = new UserDto();
        requestDto.setUsername("test");
        requestDto.setPassword("1234");

        UserDto savedUser = new UserDto();
        savedUser.setUsername("test");

        when(userService.register(any(UserDto.class))).thenReturn(savedUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입에 성공하였습니다."))
                .andExpect(jsonPath("$.data.username").value("test"));
    }

    @Test
    void testRegister_Fail_DuplicateUsername() throws Exception {
        // given
        UserDto requestDto = new UserDto();
        requestDto.setUsername("test");
        requestDto.setPassword("1234");

        when(userService.register(any(UserDto.class)))
                .thenThrow(new IllegalArgumentException("이미 존재하는 아이디입니다."));

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

    }

    @Test
    void testLogin_Success() throws Exception {
        // given
        UserDto requestDto = new UserDto();
        requestDto.setUsername("test");
        requestDto.setPassword("1234");

        UserDto userDto = new UserDto();
        userDto.setUsername("test");
        userDto.setPassword("encodedPassword");

        when(userService.login(any(UserDto.class))).thenReturn(userDto);

        MockHttpSession session = new MockHttpSession();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .session(session)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인에 성공하였습니다."))
                .andExpect(jsonPath("$.data.username").value("test"));

        // 세션 저장 검증
        Object loginUser = session.getAttribute("loginUser");
        assert loginUser != null;
    }

    @Test
    void testLogin_Fail_UserNotFound() throws Exception {
        // given
        UserDto request = new UserDto("unknown", "1234");

        when(userService.login(any(UserDto.class)))
                .thenThrow(new IllegalArgumentException("존재하지 않는 사람입니다."));

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사람입니다."));
    }

    @Test
    void testLogin_Fail_WrongPassword() throws Exception {
        // given
        UserDto request = new UserDto("testuser", "wrongpass");

        when(userService.login(any(UserDto.class)))
                .thenThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다."));

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    void testLogout_Success() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", new UserDto("testuser", "encodedPwd"));

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/logout")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃에 성공하였습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        // 세션이 무효화되었는지 확인
        assert session.isInvalid();
    }

    @Test
    void testLogout_WithoutLogin() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession(); // 로그인 사용자 없음

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/logout")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk()) // 로그아웃은 로그인 여부와 무관하게 성공 처리 가능
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃에 성공하였습니다."));
    }
}