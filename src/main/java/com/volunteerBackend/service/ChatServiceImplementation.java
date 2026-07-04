package com.volunteerBackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.stereotype.Service;

import com.volunteerBackend.DTO.ChatDTO;
import com.volunteerBackend.model.Chat;
import com.volunteerBackend.model.User;
import com.volunteerBackend.repository.ChatRepository;
import com.volunteerBackend.type.ChatType;
import com.volunteerBackend.type.UserRole;
import com.volunteerBackend.mapper.ChatMapper;

@Service
public class ChatServiceImplementation implements ChatService {

    
    private ChatRepository chatRepository;
    
    private ChatMapper chatMapper;

    @Override
    public Chat createChatAI(User reqUser) {
        Chat chat = new Chat();
        chat.setCreatedAt(LocalDateTime.now());
        chat.setSessionId(UUID.randomUUID().toString());
        chat.setChatName("AI Chat");
        Chat savedChat = chatRepository.save(chat);
        return savedChat;
    }

    @Override
    public Boolean findChatById(Integer chatId, User currentUser) throws Exception {
        Optional<Chat> opt = chatRepository.findById(chatId);
        if (opt.isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public List<ChatDTO> getChatWithUser(User reqUser) {
        // Tìm user hiện tại
        User currentUser = reqUser;
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            {
                throw new IllegalArgumentException("User không có quyền truy cập");
            }           
        }
        Optional<List<Chat>> optionalChat = chatRepository.findByChatType(ChatType.CUSTOMER);
        List<Chat> chatList = optionalChat.orElseThrow(() -> new IllegalArgumentException("Chat không tồn tại"));
        return chatMapper.toDTOList(chatList);
    }

    @Override
    public ChatDTO getChatWithAI(User reqUser) {
        // Tìm user hiện tại
        User currentUser = reqUser;
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            {
                throw new IllegalArgumentException("User không có quyền truy cập");
            }
            
        }       
        Optional<Chat> optionalChat = chatRepository.findByUserAndChatType(currentUser, ChatType.AI);
        Chat chat = optionalChat.orElseThrow(() -> new IllegalArgumentException("Chat không tồn tại"));
        // Kiểm tra user có thuộc chat này không
        boolean isParticipant = chat.getUser().equals(currentUser);
        if (!isParticipant) {
            throw new IllegalArgumentException("User không có quyền truy cập chat này");
        }
        System.out.println("DONEEEE");
        return chatMapper.toDTO(chat);
    }

    @Override
    public ChatDTO getChatWithUserContactWithAdmin(User reqUser) {
        User currentUser = reqUser;
        Optional<Chat> optionalChat = chatRepository.findByUserAndChatType(currentUser, ChatType.CUSTOMER);
        if(optionalChat.isEmpty()){
            Chat chat = new Chat();
            chat.setChatType(ChatType.CUSTOMER);
            chat.setCreatedAt(LocalDateTime.now());
            chat.setSessionId(UUID.randomUUID().toString());
            chat.setUser(currentUser);
            chatRepository.save(chat);
            return chatMapper.toDTO(chat);
        }
        Chat chat = optionalChat.get();
        // Kiem tra user co thuoc chat nay khong
        boolean isParticipant = chat.getUser().equals(currentUser);
        if (!isParticipant) {
            throw new IllegalArgumentException("User não có quyen truy cap chat nay");
        }
        return chatMapper.toDTO(chat);
    }

    @Override
    public ChatDTO getChatWithUserContactWithAI(User reqUser) {
        User currentUser = reqUser;
        Optional<Chat> optionalChat = chatRepository.findByUserAndChatType(currentUser, ChatType.AI);
        if(optionalChat.isEmpty()){
            Chat chat = new Chat();
            chat.setChatType(ChatType.AI);
            chat.setCreatedAt(LocalDateTime.now());
            chat.setSessionId(UUID.randomUUID().toString());
            chat.setUser(currentUser);
            chatRepository.save(chat);
            return chatMapper.toDTO(chat);
        }
        Chat chat = optionalChat.get();
        // Kiem tra user co thuoc chat nay khong
        boolean isParticipant = chat.getUser().equals(currentUser);
        if (!isParticipant) {
            throw new IllegalArgumentException("User não có quyen truy cap chat nay");
        }
        return chatMapper.toDTO(chat);
    }
}
