package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import com.example.demo.event.RegistrationCompleteEvent;
import com.example.demo.model.PasswordModel;
import com.example.demo.model.UserModel;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping
    public User registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user, applicationUrl(request)
        ));
        return user;
    }

   @GetMapping("/verify")
    public String verifyRegistration(@RequestParam("token") String token){
        String result = userService.validateVerificationToken(token);

        if(result.equalsIgnoreCase("valid")){
            return "User verified successfully";
        }
        return "Invalid User";
    }

    @GetMapping("/resendToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,HttpServletRequest request){
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);

        User user = verificationToken.getUser();
        resendVerificationTokenMail(user,applicationUrl(request),verificationToken.getToken());
        return "Verification link sent";

    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel,HttpServletRequest request){
        User user = userService.findUserByEmail(passwordModel.getEmail());

        String url = "";

        if(user != null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url = passwordResetTokenMail(user,applicationUrl(request),token);
        }

        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,@RequestBody PasswordModel passwordModel){
        String result = userService.validatePasswordResetToken(token);

        if(!result.equalsIgnoreCase("valid")){
            return "Invalid token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()){
            userService.changePassword(user.get(),passwordModel.getNewPassword());
            return "Password reset successfully";
        }else{
            return "Invalid token";
        }

    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl + "/register/savePassword?token=" + token;

        //send verification
        log.info("click the link to reset your password: {}",url);

        return url;
    }

    private void resendVerificationTokenMail(User user, String applicationUrl,String token) {
        String url = applicationUrl + "/register/verify?token=" + token;

        //send verification
        log.info("click the link to verify your account: {}",url);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }
}
