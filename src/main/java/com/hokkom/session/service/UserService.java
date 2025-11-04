package com.hokkom.session.service;

import com.hokkom.session.dto.UserDto;
import com.hokkom.session.entity.User;
import com.hokkom.session.entity.UserRoleType;
import com.hokkom.session.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    @Transactional
    public UserDto register(UserDto userDto) {
        if(userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .roleType(UserRoleType.USER)
                .build();

        User savedUser = userRepository.save(user);

        return new UserDto(savedUser.getUsername(), savedUser.getPassword());
    }

    // 로그인
    @Transactional(readOnly = true)
    public UserDto login(UserDto userDto) {
        User user = userRepository.findByUsername(userDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사람입니다."));
        if(!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("패스워드가 일치하지 않습니다.");
        }
        return new UserDto(user.getUsername(), user.getPassword());
    }

}
