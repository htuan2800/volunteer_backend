package com.volunteerBackend.controller;

import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.volunteerBackend.DTO.CampaignSummaryDTO;
import com.volunteerBackend.DTO.DashboardOfUserDTO;
import com.volunteerBackend.DTO.UserDTO;
import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.mapper.CampaignSummaryMapper;
import com.volunteerBackend.mapper.UserMapper;
import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.User;
import com.volunteerBackend.repository.UserRepository;
import com.volunteerBackend.request.UserRequest;
import com.volunteerBackend.response.InfoResponse;
import com.volunteerBackend.service.CampaignService;
import com.volunteerBackend.service.UserService;
import com.volunteerBackend.type.UserRole;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    
    private final UserRepository userRepository;

    
    private final UserService userService;

    
    CampaignService campaignService;

    
    private CampaignSummaryMapper campaignSummaryMapper;

    
    private UserMapper userMapper;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable("userId") Integer id) throws UserException {
        User user = userService.findUserById(id);
        UserDTO UserDTO = userMapper.toDTO(user);
        return new ResponseEntity<>(UserDTO, HttpStatus.OK);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserDTO>> searchUser(
            @RequestParam("query") String param,
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        List<User> users = userService.searchUser(param);
        List<UserDTO> UserDTOs = userMapper.toDTOList(users);
        return new ResponseEntity<>(UserDTOs, HttpStatus.OK);
    }

    @GetMapping("/users/profile")
    public ResponseEntity<InfoResponse<UserDTO>> getUserFromToken(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        UserDTO UserDTO = userMapper.toDTO(user);
        InfoResponse<UserDTO> userResponse = new InfoResponse<>(true, "User found", UserDTO);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/users/dashboard/{userId}")
    public ResponseEntity<InfoResponse<DashboardOfUserDTO>> getDashboardOfUser(@PathVariable Integer userId) throws UserException {
        User user = userService.findUserById(userId);
        DashboardOfUserDTO dashboard = userService.getDashboardOfUser(user);
        InfoResponse<DashboardOfUserDTO> dashboardResponse = new InfoResponse<>(true, "Dashboard found", dashboard);
        return new ResponseEntity<>(dashboardResponse, HttpStatus.OK);
    }

    @GetMapping("/users/dashboard/campaigns/{userId}")
    public ResponseEntity<List<CampaignSummaryDTO>> getDashboardCampaignsOfUser(@PathVariable Integer userId) throws UserException {
        User user = userService.findUserById(userId);
        List<Campaign> campaigns = campaignService.getCampaignsOfUser(user);
        List<CampaignSummaryDTO> campaignDTOs = campaignSummaryMapper.toDTOListBasicNotStoryInfo(campaigns);
        return new ResponseEntity<>(campaignDTOs, HttpStatus.OK);
    }

    @PutMapping("/users/update-info")
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal String email,
            @RequestBody UserRequest user) throws UserException {
        User existingUser = userService.findUserByEmail(email);
        boolean isSuccess = userService.updateUser(user, existingUser);
        UserDTO UserDTO = userMapper.toDTO(existingUser);
        InfoResponse<UserDTO> userResponse = new InfoResponse<>(isSuccess, "User found", UserDTO);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<User> users = userRepository.findAllByRoleNot(UserRole.ADMIN);
        List<UserDTO> UserDTOs = userMapper.toDTOList(users);
        return new ResponseEntity<>(UserDTOs, HttpStatus.OK);
    }

    @PostMapping("/admin/users/add_user")
    public ResponseEntity<?> addUser(@RequestBody User user) throws UserException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new UserException("Phone number already exists");
        }
        boolean isSuccess = userService.createUser(user);
        return new ResponseEntity<>(isSuccess, HttpStatus.CREATED);
    }

    @PutMapping("/admin/users/update-info/{userId}")
    public ResponseEntity<?> updateUserByAdmin(
            @PathVariable Integer userId,
            @RequestBody UserRequest user) throws UserException {
        boolean isSuccess = userService.updateUserByAdmin(user, userId);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }

    @PutMapping("/admin/users/{userId}/active")
    public ResponseEntity<Map<String, Boolean>> setActive(@PathVariable Integer userId) throws UserException {
        boolean isSuccess = userService.changeActiveUser(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSuccess", isSuccess);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/admin/users/{userId}/delete")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable Integer userId)
            throws UserException {
        boolean isSuccess = userService.deleteUser(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSuccess", isSuccess);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
