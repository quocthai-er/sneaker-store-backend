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
            loginResponse.setRole(user.getRole());
            loginResponse.setGender(user.getGender());
        }
        return loginResponse;
    }

    public UserResponse toUserRes(User user) {
        UserResponse userRes = new UserResponse();
        if (user != null) {
            userRes.setId(user.getId());
            userRes.setName(user.getName());
            userRes.setEmail(user.getEmail());
            userRes.setRole(user.getRole());
            userRes.setState(user.getState());
            userRes.setGender(user.getGender());
            userRes.setPhone(user.getPhone());
            userRes.setAddress(user.getAddress());
            userRes.setProvince(user.getProvince());
            userRes.setDistrict(user.getDistrict());
            userRes.setWard(user.getWard());
        }
        return userRes;
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
                    registerRequest.getAddress(), ConstantsConfig.ROLE_USER,
                    gender, ConstantsConfig.USER_STATE_UNVERIFIED, EProvider.LOCAL);
        }
        return null;
    }

}
