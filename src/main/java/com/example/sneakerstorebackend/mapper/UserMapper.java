package com.example.sneakerstorebackend.mapper;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.payloads.request.RegisterRequest;
import com.example.sneakerstorebackend.entity.user.EGender;
import com.example.sneakerstorebackend.entity.user.EProvider;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.domain.payloads.response.LoginResponse;
import com.example.sneakerstorebackend.domain.payloads.response.UserResponse;
import com.example.sneakerstorebackend.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserMapper {

    public LoginResponse toLoginRes(User user) {
        LoginResponse loginResponse = new LoginResponse();
        if (user != null) {
            loginResponse.setId(user.getId());
            loginResponse.setName(user.getName());
            loginResponse.setEmail(user.getEmail());
            loginResponse.setAvatar(user.getAvatar());
            loginResponse.setRole(user.getRole());
            loginResponse.setGender(user.getGender());
        }
        return loginResponse;
    }

    public UserResponse toUserRes(User user) {
        UserResponse userResponse = new UserResponse();
        if (user != null) {
            userResponse.setId(user.getId());
            userResponse.setName(user.getName());
            userResponse.setEmail(user.getEmail());
            userResponse.setAvatar(user.getAvatar());
            userResponse.setRole(user.getRole());
            userResponse.setState(user.getState());
            userResponse.setGender(user.getGender());
            userResponse.setPhone(user.getPhone());
            userResponse.setAddress(user.getAddress());
            userResponse.setProvince(user.getProvince());
            userResponse.setDistrict(user.getDistrict());
            userResponse.setWard(user.getWard());
        }
        return userResponse;
    }

    public User toUser(RegisterRequest registerRequest) {
        if (registerRequest != null) {
            EGender gender;
            if (!StringUtils.isPhoneNumberFormat(registerRequest.getPhone()))
                throw new AppException(400, "Phone number is invalid!");
            try {
                gender = EGender.valueOf(registerRequest.getGender().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new AppException(400, "Gender is invalid!");
            }
            return new User(registerRequest.getName(), registerRequest.getEmail(), registerRequest.getPassword(), registerRequest.getPhone(),
                    registerRequest.getProvince(), registerRequest.getDistrict(), registerRequest.getWard(),
                    registerRequest.getAddress(), ConstantsConfig.ROLE_USER, null,
                    gender, ConstantsConfig.USER_STATE_UNVERIFIED, EProvider.LOCAL);
        }
        return null;
    }

}
