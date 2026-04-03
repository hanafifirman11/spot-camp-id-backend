package com.spotcamp.authuser.api.mapper;

import com.spotcamp.authuser.api.dto.UserInfoDto;
import com.spotcamp.authuser.api.dto.UserProfileResponse;
import com.spotcamp.authuser.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User entities to DTOs
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Map User entity to UserInfoDto
     */
    UserInfoDto toUserInfoDto(User user);

    /**
     * Map User entity to UserProfileResponse
     */
    @Mapping(source = "avatarUrl", target = "avatar")
    UserProfileResponse toUserProfileResponse(User user);
}