package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.CloudinaryConfig;
import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.ChangePasswordRequest;
import com.example.sneakerstorebackend.domain.payloads.request.ChangeResetPasswordRequest;
import com.example.sneakerstorebackend.domain.payloads.request.RegisterRequest;
import com.example.sneakerstorebackend.domain.payloads.request.UserRequest;
import com.example.sneakerstorebackend.domain.payloads.response.OrderResponse;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.domain.payloads.response.UserResponse;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.entity.user.EGender;
import com.example.sneakerstorebackend.entity.user.EProvider;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.mapper.OrderMapper;
import com.example.sneakerstorebackend.mapper.UserMapper;
import com.example.sneakerstorebackend.repository.UserRepository;
import com.example.sneakerstorebackend.service.UserService;
import com.example.sneakerstorebackend.util.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final CloudinaryConfig cloudinary;

    private final OrderMapper orderMapper;


    @Transactional
    @Override
    public ResponseEntity<?> updatePasswordReset(String id, ChangeResetPasswordRequest ChangeResetPasswordRequest) {
        Optional<User> user = userRepository.findUserByIdAndState(id, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent() && user.get().getToken() != null) {
            if (!user.get().getProvider().equals(EProvider.LOCAL)) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Your account is " +
                    user.get().getProvider() + " account");
            if (ChangeResetPasswordRequest.getTempPassword().equals(user.get().getToken().getOtp())) {
                user.get().setPassword(passwordEncoder.encode(ChangeResetPasswordRequest.getNewPassword()));
                user.get().setToken(null);
                userRepository.save(user.get());
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Change password success", ""));
            } else throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Your otp is wrong");
        }
        throw new NotFoundException("Can not found user with id " + id + " is activated");
    }

    @Override
    public ResponseEntity<?> findUserById(String id) {
        Optional<User> user = userRepository.findUserByIdAndState(id, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent()) {
            UserResponse userResponse = userMapper.toUserRes(user.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get user success", userResponse));
        }
        throw new NotFoundException("Can not found user with id " + id );
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateUser(String id, UserRequest userRequest) {
        Optional<User> user;
        if (userRequest.getState() == null)
            user = userRepository.findUserByIdAndState(id, ConstantsConfig.USER_STATE_ACTIVATED);
        else user = userRepository.findById(id);
        if (user.isPresent()) {
            updateUserProcess(userRequest, user.get());
            userRepository.save(user.get());
            UserResponse res = userMapper.toUserRes(user.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Update user success", res));
        }
        throw new NotFoundException("Can not found user with id " + id );
    }

    @Override
    public ResponseEntity<?> updatePassword(String id, ChangePasswordRequest changePasswordRequest) {
        Optional<User> user = userRepository.findUserByIdAndState(id, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent()) {
            if (!user.get().getProvider().equals(EProvider.LOCAL)) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Your account is " +
                    user.get().getProvider() + " account");
            if (passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.get().getPassword())
                    && !changePasswordRequest.getNewPassword().equals(changePasswordRequest.getOldPassword())) {
                user.get().setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                userRepository.save(user.get());
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Change password success", ""));
            } else throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Your old password is wrong" +
                    " or same with new password");
        }
        throw new NotFoundException("Can not found user with id " + id + " is activated");
    }

    @Override
    public ResponseEntity<?> getUserOrderHistory(String id) {
        Optional<User> user = userRepository.findUserByIdAndState(id, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent()) {
            Comparator<Order> comparatorDesc = Comparator.comparing(Order::getCreatedDate).reversed();
            List<Order> orders = user.get().getOrders();
            orders.sort(comparatorDesc);
            List<OrderResponse> resList = orders.stream().map(orderMapper::toOrderRes).collect(Collectors.toList());
            if (resList.isEmpty()) throw new NotFoundException("Can not found any orders");
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get order history of user success", resList));
        }
        throw new NotFoundException("Can not found user with id " + id );    }

    @Override
    public ResponseEntity<?> findAll(String state, Pageable pageable) {
        Page<User> users;
        if (state.equalsIgnoreCase(ConstantsConfig.USER_STATE_ACTIVATED) ||
                state.equalsIgnoreCase(ConstantsConfig.USER_STATE_DEACTIVATED) ||
                state.equalsIgnoreCase(ConstantsConfig.USER_STATE_UNVERIFIED))
            users = userRepository.findAllByState(state.toLowerCase(), pageable);
        else users = userRepository.findAll(pageable);
        List<UserResponse> userResList = users.stream().map(userMapper::toUserRes).collect(Collectors.toList());
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", userResList);
        resp.put("totalQuantity", users.getTotalElements());
        resp.put("totalPage", users.getTotalPages());
        if (userResList.size() > 0)
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get all user success", resp));
        throw new NotFoundException("Can not found any user");
    }

    @Override
    public ResponseEntity<?> addUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(HttpStatus.CONFLICT.value(), "Email already exists");
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        User user = userMapper.toUser(request);
        if (user != null) {
            if (request.getRole().toUpperCase(Locale.ROOT).equals(ConstantsConfig.ROLE_STAFF) ||
                    request.getRole().toUpperCase(Locale.ROOT).equals(ConstantsConfig.ROLE_USER))
                user.setRole(request.getRole().toUpperCase());
            else throw new NotFoundException("Can not found role: "+ request.getRole());
            user.setState(ConstantsConfig.USER_STATE_ACTIVATED);
            try {
                userRepository.insert(user);
            } catch (Exception e){
                throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(true, "Add user successfully ", "")
        );
    }

    @Override
    public ResponseEntity<?> updateUserAvatar(String id, MultipartFile file) {
        Optional<User> user = userRepository.findUserByIdAndState(id, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent()) {
            if (file != null && !file.isEmpty()) {
                try {
                    String imgUrl = cloudinary.uploadImage(file, user.get().getAvatar());
                    user.get().setAvatar(imgUrl);
                    userRepository.save(user.get());
                } catch (IOException e) {
                    throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Error when upload image");
                }
            }
            UserResponse response = userMapper.toUserRes(user.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Update user success", response));
        }
        throw new NotFoundException("Can not found user with id " + id );
    }

    public void updateUserProcess(UserRequest input, User save) {
        if (input != null) {
            if (!input.getName().isBlank())
                save.setName(input.getName());
            if (input.getProvince() > 0)
                save.setProvince(input.getProvince());
            if (input.getDistrict() > 0)
                save.setDistrict(input.getDistrict());
            if (input.getWard() > 0)
                save.setWard(input.getWard());
            if (!input.getAddress().isBlank())
                save.setAddress(input.getAddress());
            if (!input.getPhone().isBlank() && StringUtils.isPhoneNumberFormat(input.getPhone()))
                save.setPhone(input.getPhone());
            else throw new AppException(HttpStatus.BAD_REQUEST.value(), "Phone number is invalid!");
            if (!input.getGender().isBlank())
                try {
                    save.setGender(EGender.valueOf(input.getGender().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    throw new AppException(HttpStatus.BAD_REQUEST.value(), "Gender is invalid!");
                }
            if (input.getState() != null && !input.getState().isEmpty())
                if (input.getState().equals(ConstantsConfig.USER_STATE_ACTIVATED) ||
                        input.getState().equals(ConstantsConfig.USER_STATE_DEACTIVATED))
                    save.setState(input.getState());
                else throw new AppException(HttpStatus.BAD_REQUEST.value(), "State is invalid!");
        }
    }
}
