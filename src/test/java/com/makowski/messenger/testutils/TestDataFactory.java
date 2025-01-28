package com.makowski.messenger.testutils;

import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.entity.Message;
import com.makowski.messenger.entity.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TestDataFactory {

    public static Message createTestMessage() {
        Message message = new Message();
        message.setId(1L);
        message.setSenderId(1L);
        message.setReceiverId(Set.of(2L));
        message.setPermanent(false);
        message.setContent("Test message");
        return message;
    }

    public static Chat createTestChat() {
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMessages(new ArrayList<>());
        chat.setMembers(new HashSet<>());
        return chat;
    }

    public static User createTestUser() {
        return createNewUser(1L, "username1", "firstName1", "surname1");
    }

    public static User createAnotherTestUser() {
        return createNewUser(2L, "username2", "firstName2", "surname2");
    }


    private static User createNewUser(Long id, String username, String firstName, String surname) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setSurname(surname);
        user.setPassword("password");
        user.setContactList(new HashSet<>());
        user.setChats(new ArrayList<>());
        return user;
    }
}