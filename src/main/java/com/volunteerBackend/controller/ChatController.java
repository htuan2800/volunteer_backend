package com.volunteerBackend.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.ChatDTO;
import com.volunteerBackend.model.User;
import com.volunteerBackend.service.ChatService;
import com.volunteerBackend.service.UserService;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserService userService;

    // @PostMapping("/api/chats")
    // public Chat createChat(@RequestHeader("Authorization") String jwt, @RequestBody CreateChatRequest req)
    //         throws Exception {
    //     User reqUser = userService.findUserByJwt(jwt);
    //     Chat chat = chatService.createChatAI(reqUser);
    //     return chat;
    // }

    @GetMapping("/admin/chats/ai")
    public ResponseEntity<ChatDTO> getChatWithAI(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.findUserByJwt(jwt);
        ChatDTO chat = chatService.getChatWithAI(reqUser);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @GetMapping("/admin/chats/users")
    public ResponseEntity<List<ChatDTO>> getChatWithUser(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.findUserByJwt(jwt);
        List<ChatDTO> chat = chatService.getChatWithUser(reqUser);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @GetMapping("/chats/admin-contact")
    public ResponseEntity<ChatDTO> getChatWithUserContactWithAdmin(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.findUserByJwt(jwt);
        ChatDTO chat = chatService.getChatWithUserContactWithAdmin(reqUser);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @GetMapping("/chats/ai-contact")
    public ResponseEntity<ChatDTO> getChatWithUserContactWithAI(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.findUserByJwt(jwt);
        ChatDTO chat = chatService.getChatWithUserContactWithAI(reqUser);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }
}