package com.hokkom.session.controller;

import com.hokkom.session.dto.ApiResponse;
import com.hokkom.session.dto.UserDto;
import com.hokkom.session.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody UserDto userDto) {
        UserDto savedUser = userService.register(userDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "회원가입에 성공하였습니다.", savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@RequestBody UserDto userDto, HttpSession session) {
        UserDto loginUser = userService.login(userDto);
        session.setAttribute("loginUser", loginUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "로그인에 성공하였습니다.", loginUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new ApiResponse<>(true, "로그아웃에 성공하였습니다.", null));
    }

}
