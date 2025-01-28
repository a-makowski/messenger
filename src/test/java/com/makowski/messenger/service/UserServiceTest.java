package com.makowski.messenger.service;

import com.makowski.messenger.dto.ChatDto;
import com.makowski.messenger.dto.PasswordDto;
import com.makowski.messenger.dto.UserDto;
import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.entity.User;
import com.makowski.messenger.exception.*;
import com.makowski.messenger.repository.UserRepository;

import com.makowski.messenger.testutils.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;
    @Mock
    UserRepository userRepository;
    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    void createUser_ReturnsUserDto_WhenUserIsCreated() {
        User user = TestDataFactory.createTestUser();

        when(userRepository.existsByUsernameIgnoreCase(user.getUsername())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto result = userService.createUser(user);

        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getSurname(), result.getSurname());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenUsernameIsAlreadyTaken() {
        User user = TestDataFactory.createTestUser();

        when(userRepository.existsByUsernameIgnoreCase(user.getUsername())).thenReturn(true);

        assertThrows(InvalidRequestException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_ReturnsUser_WhenUserExists() {
        User user = TestDataFactory.createTestUser();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User result = userService.findByUsername("username1");

        assertEquals(user, result);
    }

    @Test
    void findByUsername_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findByUsername("username"));
    }

    @Test
    void updateUser_ReturnsUpdatedUserDto() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        UserDto userDto = new UserDto(1L,"username1", "newFirstName", "newSurname");

        when(userRepository.findByUsername("username1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.updateUser(userDto);

        assertEquals(userDto.getFirstName(), result.getFirstName());
        assertEquals(userDto.getSurname(), result.getSurname());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ChangesPassword_WhenAllPasswordsMatches() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        PasswordDto passwordDto = new PasswordDto("password", "newPassword", "newPassword");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())).thenReturn(true);
        when(bCryptPasswordEncoder.encode(passwordDto.getNewPassword())).thenReturn("encodedPassword");

        userService.changePassword(passwordDto);

        assertEquals("encodedPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ThrowsException_WhenNewPasswordsDoesNotMatch() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        PasswordDto passwordDto = new PasswordDto("password", "newPassword", "wrongNewPassword");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())).thenReturn(true);

        assertThrows(PasswordNotEqualsException.class, () -> userService.changePassword(passwordDto));
    }

    @Test
    void changePassword_ThrowsException_WhenOldPasswordDoesNotMatch() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        PasswordDto passwordDto = new PasswordDto("wrongPassword", "newPassword", "newPassword");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> userService.changePassword(passwordDto));
    }

    @Test
    void getUserDto_ReturnsDto_WhenUserExists() {
        User user = TestDataFactory.createTestUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserDto(1L);

        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getSurname(), result.getSurname());
    }

    @Test
    void getUserDto_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserDto(1L));
    }

    @Test
    void getUsersChats_ReturnsListOfChatsDto_WhenChatsExist() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        User chatMember = TestDataFactory.createAnotherTestUser();
        Chat chat = TestDataFactory.createTestChat();
        chat.setMembers(Set.of(user, chatMember));
        user.setChats(List.of(chat));

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(chatMember));

        List<ChatDto> result = userService.getUsersChats();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getChatId());
        assertEquals(2, result.get(0).getMembers().size());
    }

    @Test
    void getUsersChats_ThrowsException_WhenUserHaveNoChats() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(EmptyListException.class, () -> userService.getUsersChats());
    }

    @Test
    void findUser_ReturnsListOfUserDto_WhenUsersAreFound() {
        User user1 = TestDataFactory.createTestUser();
        User user2 = TestDataFactory.createAnotherTestUser();
        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        Set<UserDto> result = userService.findUser("ame2");

        assertEquals(1, result.size());
        assertEquals(user2.getUsername(), result.iterator().next().getUsername());
    }

    @Test
    void findUser_ThrowsException_WhenSearchPhraseIsBlank() {
        assertThrows(InvalidRequestException.class, () -> userService.findUser(" "));
    }

    @Test
    void findUser_ThrowsException_WhenNothingFound() {
        User user = TestDataFactory.createTestUser();

        when(userRepository.findAll()).thenReturn(Set.of(user));

        assertThrows(NoSuchUserException.class, () -> userService.findUser("test"));
    }

    @Test
    void addToContactList_ReturnsUpdatedContactList_WhenUserExist() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        User contactUser = TestDataFactory.createAnotherTestUser();

        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findById(2L)).thenReturn(Optional.of(contactUser));

        Set<UserDto> result = userService.addToContactList(2L);

        assertEquals(1, result.size());
        assertEquals(contactUser.getUsername(), result.iterator().next().getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void addToContactList_ThrowsException_WhenUserIsContactListOwner() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(InvalidRequestException.class, () -> userService.addToContactList(1L));
    }

    @Test
    void addToContactList_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.addToContactList(1L));
    }

    @Test
    void deleteFromContactList_ReturnsUpdatedList_WhenUserWasDeleted() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        User contactUser = TestDataFactory.createAnotherTestUser();
        user.setContactList(new HashSet<>(Set.of(2L, 3L)));

        when(userRepository.existsById(3L)).thenReturn(true);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findById(2L)).thenReturn(Optional.of(contactUser));

        Set<UserDto> result = userService.deleteFromContactList(3L);

        assertEquals(1, result.size());
        assertEquals(contactUser.getUsername(), result.iterator().next().getUsername());
    }

    @Test
    void deleteFromContactList_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.addToContactList(1L));
    }

    @Test
    void deleteFromContactList_ThrowsException_WhenUserIsNotOnContactList() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        user.setContactList(Set.of(2L, 3L));

        when(userRepository.existsById(4L)).thenReturn(true);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(InvalidRequestException.class, () -> userService.deleteFromContactList(4L));
    }

    @Test
    void getMyContactList_ReturnsContactList_WhenUserHasContactList() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();
        User contactUser = TestDataFactory.createAnotherTestUser();
        user.setContactList(Set.of(2L));

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(contactUser));

        Set<UserDto> result = userService.getMyContactList();

        assertEquals(1, result.size());
        assertEquals(contactUser.getUsername(), result.iterator().next().getUsername());
    }

    @Test
    void getMyContactList_ThrowsException_WhenListIsEmpty() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username1", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = TestDataFactory.createTestUser();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(EmptyListException.class, () -> userService.getMyContactList());
    }
}