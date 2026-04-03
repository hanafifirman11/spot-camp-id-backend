package com.spotcamp.module.authuser.mapper;

import com.spotcamp.module.authuser.dto.UserInfoDTO;
import com.spotcamp.module.authuser.dto.UserProfileResponseDTO;
import com.spotcamp.module.authuser.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User entities to DTOs
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Map User entity to UserInfoDTO
     */
    UserInfoDTO toUserInfoDto(User user);

    /**
     * Map User entity to UserProfileResponseDTO
     */
    @Mapping(source = "avatarUrl", target = "avatar")
    UserProfileResponseDTO toUserProfileResponse(User user);
}