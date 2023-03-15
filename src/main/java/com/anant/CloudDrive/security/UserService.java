package com.anant.CloudDrive.security;

import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findByEmail(String email);

    List<UserDto> findAllUsers();
}
