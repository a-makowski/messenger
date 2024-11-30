package com.makowski.messenger.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.makowski.messenger.dto.ChatDto;
import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.exception.AccessDeniedException;
import com.makowski.messenger.exception.EntityNotFoundException;
import com.makowski.messenger.repository.ChatRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatService {
 
    private ChatRepository chatRepository;
    private UserService userService;    

    public List<ChatDto> getMyChats() {        
        return userService.getUsersChats();
    } 

    public Chat getChat(Long chatId) {
        return chatRepository.findById(chatId)
            .orElseThrow(() -> new EntityNotFoundException(chatId, Chat.class));
    }

    public Chat getMyChat(Long chatId) {
        if (isItProperUser(chatId)) return getChat(chatId);
            else throw new AccessDeniedException();
    }

    public void saveChat(Chat chat) {
        chatRepository.save(chat);
    }
                                                                   
    public void deleteChatIfExist(Long chatId) {                  
        if (chatRepository.existsById(chatId)) {
            if (isItProperUser(chatId)) deleteChat(chatId);
                else throw new AccessDeniedException();
        } else throw new EntityNotFoundException(chatId, Chat.class);    
    }

    public void deleteChatIfEmpty(Long chatId) { 
        if (getChat(chatId).getMessages().isEmpty()) {                   
            deleteChat(chatId);           
        }
    }

    public void deleteChat(Long chatId) {                          
        chatRepository.deleteById(chatId); 
    }

    public boolean isItProperUser (Long chatId) {
        return getChat(chatId).getMembers().contains(userService.getLoggedUser());
    }
}