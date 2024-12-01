package com.makowski.messenger.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.makowski.messenger.constants.Constants;
import org.springframework.stereotype.Service;

import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.entity.Message;
import com.makowski.messenger.entity.User;
import com.makowski.messenger.exception.AccessDeniedException;
import com.makowski.messenger.exception.EntityNotFoundException;
import com.makowski.messenger.exception.InvalidRequestException;
import com.makowski.messenger.exception.NoReceiversException;
import com.makowski.messenger.repository.MessageRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MessageService {

    private MessageRepository messageRepository;
    private UserService userService;
    private ChatService chatService;

    public Message saveMessage(Message message) {
        isItProperLength(message.getContent().length());
        User sender = userService.getLoggedUser();
        message.setSenderId(sender.getId());
        Set<User> members = new HashSet<>();
        for (Long id : message.getReceiverId()) {
            if (!id.equals(message.getSenderId())) {
                if (userService.existsById(id)) {
                    User user = userService.getUser(id);
                    members.add(user);
                } else throw new EntityNotFoundException(id, User.class);
            }
        }
        if (members.isEmpty()) throw new NoReceiversException();
        members.add(sender);
        for (Chat chat : sender.getChats()) {
            Set<User> checkMembers = chat.getMembers();
            if (checkMembers.size() == members.size() && checkMembers.containsAll(members) && members.containsAll(checkMembers)) {
                message.setChat(chat);
                break;
            }
        }
        if (message.getChat() == null) {
            Chat chat = new Chat();
            chat.setMembers(members);
            message.setChat(chat);
            chatService.saveChat(chat);
        }
        return messageRepository.save(message);
    }

    public Message updateMessage(String content, Long messageId) {
        if (content == null) throw new InvalidRequestException();
        if (content.isBlank()) throw new InvalidRequestException();
        isItProperLength(content.length());
        if (isItProperUser(messageId)) {
            Message updatedMessage = getMessage(messageId);
            updatedMessage.setContent(content);
            updatedMessage.setDateTime(LocalDateTime.now());
            return messageRepository.save(updatedMessage);
        } throw new AccessDeniedException();
    }

    public Message changeFlag(Long messageId) {
        if (isItProperUser(messageId)) {
            Message updatedMessage = getMessage(messageId);
            updatedMessage.setPermanent(!updatedMessage.isPermanent());
            return messageRepository.save(updatedMessage);
        } else throw new AccessDeniedException();
    }

    public void deleteMessage(Long id) {
        Long chatId = getMessage(id).getChat().getId();
        messageRepository.deleteById(id);
        chatService.deleteChatIfEmpty(chatId);
    }

    public void deleteMyMessage(Long messageId) {
        if (isItProperUser(messageId)) deleteMessage(messageId);
            else throw new AccessDeniedException();
    }

    public Message getMessage(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id, Message.class));
    }

    public boolean isItProperUser(Long messageId) {
        return userService.getLoggedUser().getId().equals(getMessage(messageId).getSenderId());
    }

    public void isItProperLength(int length) {
        if (length > Constants.MAX_MSG_LENGTH) throw new InvalidRequestException(length - Constants.MAX_MSG_LENGTH);
    }
}