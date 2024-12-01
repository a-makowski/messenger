package com.makowski.messenger.service;

import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.entity.Message;
import com.makowski.messenger.entity.User;
import com.makowski.messenger.exception.AccessDeniedException;
import com.makowski.messenger.exception.EntityNotFoundException;
import com.makowski.messenger.exception.InvalidRequestException;
import com.makowski.messenger.exception.NoReceiversException;
import com.makowski.messenger.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks
    MessageService messageService;
    @Mock
    MessageRepository messageRepository;
    @Mock
    UserService userService;
    @Mock
    ChatService chatService;

    @Test
    void saveMessage_ReturnsMessageAndAddsNewChat_WhenMessageIsSuccessfullyCreated() {
        User user = new User();
        user.setId(1L);
        user.setChats(new ArrayList<>());

        User receiver = new User();
        receiver.setId(2L);

        Message message = new Message();
        message.setContent("content");
        message.setId(1L);
        message.setSenderId(1L);
        message.setReceiverId(Set.of(2L));

        when(userService.getLoggedUser()).thenReturn(user);
        when(userService.existsById(2L)).thenReturn(true);
        when(userService.getUser(2L)).thenReturn(receiver);
        when(messageRepository.save(message)).thenReturn(message);

        Message result = messageService.saveMessage(message);

        assertEquals("content", result.getContent());
        assertEquals(2, result.getChat().getMembers().size());
        verify(messageRepository).save(message);
    }

    @Test
    void saveMessage_ThrowsException_WhenMessageIsTooLong() {
        String content = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789abcd";
        Message message = new Message();
        message.setContent(content);

        assertThrows(InvalidRequestException.class, () -> messageService.saveMessage(message));
    }

    @Test
    void saveMessage_ThrowsException_WhenReceiverDoesNotExist() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setContent("content");
        message.setId(1L);
        message.setReceiverId(Set.of(2L));

        when(userService.getLoggedUser()).thenReturn(user);
        when(userService.existsById(2L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> messageService.saveMessage(message));
    }

    @Test
    void saveMessage_ThrowsException_WhenThereIsNoReceiver() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setContent("content");
        message.setId(1L);
        message.setReceiverId(new HashSet<>());

        when(userService.getLoggedUser()).thenReturn(user);

        assertThrows(NoReceiversException.class, () -> messageService.saveMessage(message));
    }

    @Test
    void updateMessage_ReturnsMessage_WhenSuccessfullyUpdated() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setSenderId(1L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        Message result = messageService.updateMessage("abcd", 1L);

        assertEquals("abcd", result.getContent());
        verify(messageRepository).save(message);
    }

    @Test
    void updateMessage_ThrowsException_WhenNoContent() {
        assertThrows(InvalidRequestException.class, () -> messageService.updateMessage("", 1L));
    }

    @Test
    void updateMessage_ThrowsException_WhenContentIsBlank() {
        assertThrows(InvalidRequestException.class, () -> messageService.updateMessage("   ", 1L));
    }

    @Test
    void updateMessage_ThrowsException_WhenContentIsTooLong() {
        String content = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789abcd";
        assertThrows(InvalidRequestException.class, () -> messageService.updateMessage(content, 1L));
    }

    @Test
    void updateMessage_ThrowsException_WhenUserIsNotSender() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setSenderId(2L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThrows(AccessDeniedException.class, () -> messageService.updateMessage("abcd", 1L));
    }

    @Test
    void updateMessage_ThrowsException_WhenMessageDoesNotExist() {
        User user = new User();
        user.setId(1L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> messageService.updateMessage("abcd", 1L));
    }

    @Test
    void changeFlag_ReturnsMessage_WhenSuccessfullyUpdated() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setSenderId(1L);
        message.setPermanent(false);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        Message result = messageService.changeFlag(1L);

        assertTrue(result.isPermanent());
    }

    @Test
    void changeFlag_ThrowsException_WhenUserIsNotSender() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setSenderId(2L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThrows(AccessDeniedException.class, () -> messageService.changeFlag(1L));
    }

    @Test
    void changeFlag_ThrowsException_WhenMessageDoesNotExist() {
        User user = new User();
        user.setId(1L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> messageService.changeFlag(1L));
    }

    @Test
    void deleteMessage_DeletesMessage_WhenMessageExists() {
        Chat chat = new Chat();
        chat.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setChat(chat);

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.deleteMessage(1L);

        verify(messageRepository).deleteById(1L);
    }

    @Test
    void deleteMessage_ThrowsException_WhenMessageDoesNotExist() {
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> messageService.deleteMessage(1L));
    }

    @Test
    void deleteMyMessage_DeletesMessage_WhenMessageExistAndUserIsSender() {
        User user = new User();
        user.setId(1L);

        Chat chat = new Chat();
        chat.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setSenderId(1L);
        message.setChat(chat);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.deleteMyMessage(1L);
        verify(messageRepository).deleteById(1L);
    }

    @Test
    void deleteMyMessage_ThrowsException_WhenMessageDoesNotExist() {
        User user = new User();
        user.setId(1L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> messageService.deleteMyMessage(1L));
    }

    @Test
    void deleteMyMessage_ThrowsException_WhenUserIsNotSender() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setSenderId(2L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThrows(AccessDeniedException.class, () -> messageService.deleteMyMessage(1L));
    }

    @Test
    void isItProperUser_ReturnsTrue_WhenUserIsSender() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setSenderId(1L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        Boolean result = messageService.isItProperUser(1L);

        assertEquals(true, result);
    }

    @Test
    void isItProperUser_ReturnsFalse_WhenUserIsNotSender() {
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setId(1L);
        message.setSenderId(2L);

        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        Boolean result = messageService.isItProperUser(1L);

        assertEquals(false, result);
    }

    @Test
    void isItProperLength_DoesNothing_WhenMessageIsNotTooLong() {
        assertDoesNotThrow(() -> messageService.isItProperLength(200));
    }

    @Test
    void isItProperLength_ThrowsException_WhenMessageIsTooLong() {
        assertThrows(InvalidRequestException.class, () -> messageService.isItProperLength(201));
    }
}