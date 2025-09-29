package com.volunteerBackend.service;

import java.util.List;

import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.RegisterRequest;

public interface UserService {
    public User registerUser(RegisterRequest user) throws Exception;

    public User findUserById(Integer userId) throws UserException;

    public User findUserByEmail(String email);

    public User findUserByPhoneNumber(String phoneNumber);

    public User updateUser(User user, Integer userId) throws UserException;

    public List<User> searchUser(String query);

    public User findUserByJwt(String jwt);

    public User getUserById(Integer id);

    public boolean verifyEmail(String token);

    public void resendVerificationEmail(String email);

}
