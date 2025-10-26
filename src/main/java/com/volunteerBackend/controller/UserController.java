package com.volunteerBackend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    CampaignService campaignService;

    @Autowired
    private CampaignSummaryMapper campaignSummaryMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/api/users")
    public ResponseEntity<List<UserDTO>> getUsers(
            @RequestHeader(value = "Authorization", required = false) String jwt) {

        List<User> users = userRepository.findAllByRoleNot(UserRole.ADMIN);
        List<UserDTO> UserDTOs = userMapper.toDTOList(users);
        return new ResponseEntity<>(UserDTOs, HttpStatus.OK);
    }

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable("userId") Integer id) throws UserException {
        User user = userService.findUserById(id);
        UserDTO UserDTO = userMapper.toDTO(user);
        return new ResponseEntity<>(UserDTO, HttpStatus.OK);
    }

    @GetMapping("/api/users/search")
    public ResponseEntity<List<UserDTO>> searchUser(
            @RequestParam("query") String param,
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        List<User> users = userService.searchUser(param);
        List<UserDTO> UserDTOs = userMapper.toDTOList(users);
        return new ResponseEntity<>(UserDTOs, HttpStatus.OK);
    }

    @GetMapping("/api/users/profile")
    public ResponseEntity<InfoResponse<UserDTO>> getUserFromToken(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        UserDTO UserDTO = userMapper.toDTO(user);
        InfoResponse<UserDTO> userResponse = new InfoResponse<>(true, "User found", UserDTO);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/api/users/dashboard")
    public ResponseEntity<InfoResponse<DashboardOfUserDTO>> getDashboardOfUser(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.findUserByJwt(jwt);
        DashboardOfUserDTO dashboard = userService.getDashboardOfUser(user);
        InfoResponse<DashboardOfUserDTO> dashboardResponse = new InfoResponse<>(true, "Dashboard found", dashboard);
        return new ResponseEntity<>(dashboardResponse, HttpStatus.OK);
    }

    @GetMapping("/api/users/dashboard/campaigns")
    public ResponseEntity<List<CampaignSummaryDTO>> getDashboardCampaignsOfUser(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.findUserByJwt(jwt);
        List<Campaign> campaigns = campaignService.getCampaignsOfUser(user);
        List<CampaignSummaryDTO> campaignDTOs = campaignSummaryMapper.toDTOListBasicNotStoryInfo(campaigns);
        return new ResponseEntity<>(campaignDTOs, HttpStatus.OK);
    }

    @PostMapping("/api/users/add_user")
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

    @PutMapping("/api/users/update-info")
    public ResponseEntity<?> updateUser(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @RequestBody UserRequest user) throws UserException {
        Integer userId = userService.findUserByJwt(jwt).getId();
        boolean isSuccess = userService.updateUser(user, userId);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }

    @PutMapping("/api/users/update-info/{userId}")
    public ResponseEntity<?> updateUserByAdmin(
            @PathVariable("userId") Integer userId,
            @RequestBody UserRequest user) throws UserException {
        boolean isSuccess = userService.updateUserByAdmin(user, userId);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }

    @PutMapping("/users/{userId}/active")
    public ResponseEntity<Map<String, Boolean>> setActive(@PathVariable("userId") Integer userId) throws UserException {
        boolean isSuccess = userService.changeActiveUser(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSuccess", isSuccess);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}/delete")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable("userId") Integer userId)
            throws UserException {
        boolean isSuccess = userService.deleteUser(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSuccess", isSuccess);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
