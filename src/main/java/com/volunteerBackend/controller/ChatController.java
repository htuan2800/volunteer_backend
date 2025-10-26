package com.volunteerBackend.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.ChatDTO;
import com.volunteerBackend.model.Chat;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.CreateChatRequest;
import com.volunteerBackend.service.ChatService;
import com.volunteerBackend.service.UserService;

@RestController
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserService userService;

    @PostMapping("/api/chats")
    public Chat createChat(@RequestHeader("Authorization") String jwt, @RequestBody CreateChatRequest req)
            throws Exception {
        User reqUser = userService.findUserByJwt(jwt);
        Chat chat = chatService.createChatAI(reqUser);
        return chat;
    }

    @GetMapping("/api/chats/ai")
    public ResponseEntity<ChatDTO> getChatWithAI(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.findUserByJwt(jwt);
        ChatDTO chat = chatService.getChatWithAI(reqUser);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @GetMapping("/api/chats/users")
    public ResponseEntity<List<ChatDTO>> getChatWithUser(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.findUserByJwt(jwt);
        List<ChatDTO> chat = chatService.getChatWithUser(reqUser);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @GetMapping("/api/chats/admin/contact")
    public ResponseEntity<ChatDTO> getChatWithUserContact(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.findUserByJwt(jwt);
        ChatDTO chat = chatService.getChatWithUserContact(reqUser);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }
}