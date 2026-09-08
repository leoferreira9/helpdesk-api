package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.UserRequestDto;
import com.leonardo.helpdesk.dto.response.UserResponseDto;
import com.leonardo.helpdesk.dto.update.ChangePasswordUpdateDto;
import com.leonardo.helpdesk.dto.update.UserUpdateDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.EmailAlreadyRegistered;
import com.leonardo.helpdesk.exception.EntityNotFound;
import com.leonardo.helpdesk.mapper.UserMapper;
import com.leonardo.helpdesk.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User findUserOrThrow(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFound("User not found with ID: " + id));
    }

    @Transactional
    public UserResponseDto create(UserRequestDto requestDto) {
        if(userRepository.existsByEmail(requestDto.email())){
            throw new EmailAlreadyRegistered("Email " + requestDto.email() + " already registered");
        }

        User user = userMapper.convertToEntity(requestDto);
        user.setRole(UserRole.USER);

        String cryptographedPassword = passwordEncoder.encode(requestDto.password());
        user.setPassword(cryptographedPassword);

        User savedUser = userRepository.save(user);
        return userMapper.convertToResponseDto(savedUser);
    }

    public UserResponseDto findById(UUID id){
        User userExists = findUserOrThrow(id);
        return userMapper.convertToResponseDto(userExists);
    }

    public Page<UserResponseDto> findAll(Pageable pageable){
        Page<User> pageResult = userRepository.findAll(pageable);
        return pageResult.map(userMapper::convertToResponseDto);
    }

    @Transactional
    public UserResponseDto update(UUID id, UserUpdateDto updateDto) {
        User userExists = findUserOrThrow(id);

        if(updateDto != null){
            if(updateDto.name() != null && !updateDto.name().isBlank()){
                userExists.setName(updateDto.name());
            }

            if(updateDto.email() != null && !updateDto.email().isBlank()){
                if(userRepository.existsByEmail(updateDto.email()) && !userExists.getEmail().equals(updateDto.email())){
                    throw new EmailAlreadyRegistered("Email " + updateDto.email() + " already registered");
                }

                userExists.setEmail(updateDto.email());
            }
        }

        User updatedUser = userRepository.save(userExists);
        return userMapper.convertToResponseDto(updatedUser);
    }

    @Transactional
    public UserResponseDto updatePassword(UUID id, ChangePasswordUpdateDto changePasswordUpdateDto) {
        User userExists = findUserOrThrow(id);

        if(changePasswordUpdateDto != null){
            if(changePasswordUpdateDto.password() != null && !changePasswordUpdateDto.password().isBlank()){
                String cryptographedPassword = passwordEncoder.encode(changePasswordUpdateDto.password());
                userExists.setPassword(cryptographedPassword);
            }
        }

        User updatedUser = userRepository.save(userExists);
        return userMapper.convertToResponseDto(updatedUser);
    }

    @Transactional
    public void delete(UUID id){
        User userExists = findUserOrThrow(id);
        userExists.setActive(false);
        userRepository.save(userExists);
    }
}
