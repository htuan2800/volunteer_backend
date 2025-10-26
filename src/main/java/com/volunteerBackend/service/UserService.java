package com.volunteerBackend.service;

import java.util.List;

import com.volunteerBackend.DTO.DashboardOfUserDTO;
import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.ChangePasswordRequest;
import com.volunteerBackend.request.RegisterRequest;
import com.volunteerBackend.request.ResetPasswordRequest;
import com.volunteerBackend.request.UserRequest;

public interface UserService {
    public User registerUser(RegisterRequest user) throws Exception;

    public boolean forgotPassword(String email) throws UserException;

    public boolean changePassword(ChangePasswordRequest request, User user) throws UserException;

    public boolean resetPassword(ResetPasswordRequest request) throws UserException;

    public boolean createUser(User user) throws UserException;

    public User findUserById(Integer userId) throws UserException;

    public User findUserByEmail(String email);

    public User findUserByPhoneNumber(String phoneNumber);

    public User findByFullName(String username);

    public boolean updateUser(UserRequest user, Integer userId) throws UserException;

    public boolean updateUserByAdmin(UserRequest user, Integer userId) throws UserException;

    public List<User> searchUser(String query);

    public User findUserByJwt(String jwt);

    public User getUserById(Integer id);

    public boolean verifyEmail(String token);

    public void resendVerificationEmail(String email);

    public boolean deleteUser(Integer userId) throws UserException;

    public boolean changeActiveUser(Integer userId) throws UserException;

    public  DashboardOfUserDTO getDashboardOfUser(User user) throws UserException;
}
