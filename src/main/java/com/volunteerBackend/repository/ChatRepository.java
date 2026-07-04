package com.volunteerBackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import com.volunteerBackend.model.Chat;
import com.volunteerBackend.model.User;
import com.volunteerBackend.type.ChatType;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Query("SELECT DISTINCT c FROM Chat c JOIN c.messages m")
    List<Chat> findAllWithMessages();

    //For Admin
    Optional<List<Chat>> findByChatType(ChatType chatType);
    Optional<Chat> findByUserAndChatType(User admin, ChatType chatType);

    //For User
    Optional<Chat> findByUser(User user);

}
