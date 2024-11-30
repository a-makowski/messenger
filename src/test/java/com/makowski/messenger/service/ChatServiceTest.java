package com.makowski.messenger.service;

import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.entity.Message;
import com.makowski.messenger.entity.User;
import com.makowski.messenger.exception.AccessDeniedException;
import com.makowski.messenger.exception.EntityNotFoundException;
import com.makowski.messenger.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    ChatService chatService;
    @Mock
    ChatRepository chatRepository;
    @Mock
    UserService userService;


    @Test
    void getMyChat_ReturnsChat_WhenUserIsChatMember() {
        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);

        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMembers(new HashSet<>(Set.of(loggedUser)));

        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        Chat result = chatService.getMyChat(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getMyChat_ThrowsException_WhenUserIsNotChatMember() {
        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);

        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMembers(new HashSet<>());

        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        assertThrows(AccessDeniedException.class, () -> chatService.getMyChat(1L));
    }

    @Test
    void deleteChatIfExist_DeletesChat_WhenChatExistsAndUserIsChatMember() {
        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);

        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMembers(new HashSet<>(Set.of(loggedUser)));

        when(chatRepository.existsById(1L)).thenReturn(true);
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        chatService.deleteChatIfExist(1L);
        verify(chatRepository).deleteById(1L);
    }

    @Test
    void deleteChatIfExist_ThrowsException_WhenChatDoesNotExist() {
        when(chatRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> chatService.deleteChatIfExist(1L));
        verify(chatRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void deleteChatIfExist_ThrowsException_WhenUserIsNotChatMember() {
        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);

        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMembers(new HashSet<>());

        when(chatRepository.existsById(1L)).thenReturn(true);
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        assertThrows(AccessDeniedException.class, () -> chatService.deleteChatIfExist(1L));
        verify(chatRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void deleteChatIfEmpty_DeletesChat_WhenChatIsEmpty() {
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMessages(new ArrayList<>());

        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        chatService.deleteChatIfEmpty(1L);

        verify(chatRepository).deleteById(1L);
    }

    @Test
    void deleteChatIfEmpty_DoesNotDeleteChat_WhenChatIsNotEmpty() {
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMessages(new ArrayList<>(List.of(message)));

        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        chatService.deleteChatIfEmpty(1L);

        verify(chatRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void isItProperUser_ReturnsTrue_WhenUserIsChatMember() {
        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);

        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMembers(new HashSet<>(Set.of(loggedUser)));

        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        Boolean result = chatService.isItProperUser(1L);

        assertEquals(true, result);
    }

    @Test
    void isItProperUser_ReturnsFalse_WhenUserIsNotChatMember() {
        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);

        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMembers(new HashSet<>());

        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        Boolean result = chatService.isItProperUser(1L);

        assertEquals(false, result);
    }
}