package com.example.identityservice.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.identityservice.dto.request.UserCreationRequest;
import com.example.identityservice.dto.request.UserUpdateRequest;
import com.example.identityservice.dto.response.UserResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.enums.Role;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.example.identityservice.mapper.UserMapper;
import com.example.identityservice.repository.RoleRepository;
import com.example.identityservice.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername((request.getUsername()))) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        // user.setRoles(roles);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext().getAuthentication();
        String name = context.getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    //    kiem tra roi moi cho vao = tuong lua, thuc ra trong scope no la ROLE_ADMIN minh dung hasRole nen chi can
    // ADMIN,
    //    hoac co the dung hasAuthority('ROLE_ADMIN')
    @PreAuthorize("hasRole('ADMIN')")
    //    @PreAuthorize("hasAnyAuthority('APPROVE_POST')")
    public List<UserResponse> getUsers() {
        log.info("In method");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    // vao chay xog r moi kiem tra
    @PostAuthorize("returnObject.username==authentication.name")
    public UserResponse getUser(String id) {
        log.info("in method getUser");
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
    }
}
