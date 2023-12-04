package com.example.sneakerstorebackend.security.oauth;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.payloads.response.LoginResponse;
import com.example.sneakerstorebackend.entity.user.EGender;
import com.example.sneakerstorebackend.entity.user.EProvider;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.mapper.UserMapper;
import com.example.sneakerstorebackend.repository.UserRepository;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@AllArgsConstructor
public class Success extends SavedRequestAwareAuthenticationSuccessHandler {
    private UserRepository userRepository;
    private JwtUtils jwtUtil;
    private UserMapper userMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        EProvider provider = EProvider.valueOf(oauth2User.getOauth2ClientName().toUpperCase());

        Optional<User> user = userRepository.findUserByEmailAndState(oauth2User.getEmail(), ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isEmpty()) {
            String accessToken = processAddUser(oauth2User, provider);
            response.sendRedirect(generateRedirectURL(true, accessToken, provider, ""));
        } else {
            try {
                if (EnumUtils.isValidEnum(EProvider.class, user.get().getProvider().name()) &&
                        !user.get().getProvider().equals(EProvider.LOCAL) &&
                        provider.equals(user.get().getProvider())) {
                    String accessToken = jwtUtil.generateTokenFromUserId(user.get());
                    response.sendRedirect(generateRedirectURL(true, accessToken, provider, ""));
                } else response.sendRedirect(generateRedirectURL(false, "",
                        user.get().getProvider(), user.get().getEmail() + " already have an account with +"+
                                user.get().getProvider() +" method"));
            } catch (NullPointerException e) {
                response.sendRedirect(generateRedirectURL(false, "",
                        user.get().getProvider(), user.get().getEmail() + " already have an account with +"+
                                user.get().getProvider() +" method"));
            }
        }
    }

    public String processAddUser(CustomOAuth2User oAuth2User, EProvider provider) {
        User newUser = new User(oAuth2User.getName(), oAuth2User.getEmail(), "",
                "0909094323", 0, 0, 0, "unknown", ConstantsConfig.ROLE_USER,
                oAuth2User.getProfilePicture(), EGender.OTHER, ConstantsConfig.USER_STATE_ACTIVATED, provider);
        userRepository.save(newUser);
        String accessToken = jwtUtil.generateTokenFromUserId(newUser);
        LoginResponse res = userMapper.toLoginRes(newUser);
        res.setAccessToken(accessToken);
        return accessToken;
    }

    public String generateRedirectURL(Boolean success, String token, EProvider provider, String message) {
        logger.debug(message);
        //String CLIENT_HOST_REDIRECT = "http://localhost:3000/oauth2/redirect?token=";
        String CLIENT_HOST_REDIRECT = "https://sneakershop-pi.vercel.app/oauth2/redirect?token=";
        return CLIENT_HOST_REDIRECT + token + "&success=" + success + "&provider=" + provider.toString();
    }
}