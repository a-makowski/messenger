package com.makowski.messenger.service;

import com.makowski.messenger.dto.ChatDto;
import com.makowski.messenger.dto.PasswordDto;
import com.makowski.messenger.dto.UserDto;
import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.entity.User;
import com.makowski.messenger.exception.*;
import com.makowski.messenger.repository.UserRepository;

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
import static org.mockito.ArgumentMatchers.any;
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
        User user = new User("username", "password", "firstName", "surname");
        user.setId(1L);

        when(userRepository.existsByUsernameIgnoreCase(user.getUsername())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto result = userService.createUser(user);

        assertEquals("username", result.getUsername());
        assertEquals("firstName", result.getFirstName());
        assertEquals("surname", result.getSurname());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenUsernameIsAlreadyTaken() {
        User user = new User();
        user.setUsername("username");

        when(userRepository.existsByUsernameIgnoreCase(user.getUsername())).thenReturn(true);

        assertThrows(InvalidRequestException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_ReturnsUser_WhenUserExists() {
        User user = new User("username", "password", "firstName", "surname");
        user.setId(1L);

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User result = userService.findByUsername("username");

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
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);
        UserDto userDto = new UserDto(1L,"username", "newFirstName", "newSurname");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));
        when(userRepository.save(loggedUser)).thenReturn(loggedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(loggedUser));

        UserDto result = userService.updateUser(userDto);

        assertEquals("newFirstName", result.getFirstName());
        assertEquals("newSurname", result.getSurname());
        verify(userRepository).save(loggedUser);
    }

    @Test
    void changePassword_ChangesPassword_WhenAllPasswordsMatches() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        PasswordDto passwordDto = new PasswordDto("password", "newPassword", "newPassword");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));
        when(bCryptPasswordEncoder.matches(passwordDto.getOldPassword(), loggedUser.getPassword())).thenReturn(true);
        when(bCryptPasswordEncoder.encode(passwordDto.getNewPassword())).thenReturn("encodedPassword");

        userService.changePassword(passwordDto);

        assertEquals(loggedUser.getPassword(), "encodedPassword");
        verify(userRepository).save(loggedUser);
    }

    @Test
    void changePassword_ThrowsException_WhenNewPasswordsDoesNotMatch() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        PasswordDto passwordDto = new PasswordDto("password", "newPassword", "wrongNewPassword");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));
        when(bCryptPasswordEncoder.matches(passwordDto.getOldPassword(), loggedUser.getPassword())).thenReturn(true);

        assertThrows(PasswordNotEqualsException.class, () -> userService.changePassword(passwordDto));
    }

    @Test
    void changePassword_ThrowsException_WhenOldPasswordDoesNotMatch() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        PasswordDto passwordDto = new PasswordDto("wrongPassword", "newPassword", "newPassword");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));
        when(bCryptPasswordEncoder.matches(passwordDto.getOldPassword(), loggedUser.getPassword())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> userService.changePassword(passwordDto));
    }

    @Test
    void getUserDto_ReturnsDto_WhenUserExists() {
        User user = new User("username", "password", "firstName", "surname");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserDto(1L);

        assertEquals(result.getUserId(), 1L);
        assertEquals(result.getUsername(), "username");
        assertEquals(result.getFirstName(), "firstName");
        assertEquals(result.getSurname(), "surname");
    }

    @Test
    void getUserDto_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserDto(1L));
    }

    @Test
    void getUsersChats_ReturnsListOfChatsDto_WhenChatsExist() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        User member1 = new User("member1", "password", "membersName", "membersSurname");
        User member2 = new User("member2", "password", "membersName", "membersSurname");
        loggedUser.setId(1L);
        member1.setId(2L);
        member2.setId(3L);

        Chat chat = new Chat();
        chat.setId(1L);
        chat.setMembers(Set.of(loggedUser, member1, member2));
        loggedUser.setChats(List.of(chat));

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(loggedUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(member2));

        List<ChatDto> result = userService.getUsersChats();
        ChatDto resultChatDto = result.get(0);

        assertEquals(1, result.size());
        assertEquals(1L, resultChatDto.getChatId());
        assertEquals(3, resultChatDto.getMembers().size());
    }

    @Test
    void getUsersChats_ThrowsException_WhenUserHaveNoChats() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);
        loggedUser.setChats(new ArrayList<>());

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));

        assertThrows(EmptyListException.class, () -> userService.getUsersChats());
    }

    @Test
    void findUser_ReturnsListOfUserDto_WhenUsersAreFound() {
        User user1 = new User("username", "password", "firstName", "surname");
        User user2 = new User("Nanny", "password", "Mary", "Poppins");
        User user3 = new User("example", "password", "anotherFirstName", "anotherSurname");
        user1.setId(1L);
        user2.setId(2L);
        user3.setId(3L);
        List<User> users = List.of(user1, user2, user3);

        when(userRepository.findAll()).thenReturn(users);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        Set<UserDto> result = userService.findUser("Mary Poppin");
        UserDto resultUser = result.iterator().next();

        assertEquals(1, result.size());
        assertEquals("Nanny", resultUser.getUsername());
    }

    @Test
    void findUser_ThrowsException_WhenSearchPhraseIsBlank() {
        assertThrows(InvalidRequestException.class, () -> userService.findUser("   "));
    }

    @Test
    void findUser_ThrowsException_WhenSearchPhraseIsEmpty() {
        assertThrows(InvalidRequestException.class, () -> userService.findUser(""));
    }

    @Test
    void findUser_ThrowsException_WhenNothingFound() {
        User user = new User("username", "password", "firstName", "surname");

        when(userRepository.findAll()).thenReturn(Set.of(user));

        assertThrows(NoSuchUserException.class, () -> userService.findUser("something"));
    }

    @Test
    void addToContactList_ReturnsUpdatedContactList_WhenUserExist() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);
        loggedUser.setContactList(new HashSet<>());
        User contactUser = new User("contact", "password", "name", "surname");
        contactUser.setId(2L);

        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));
        when(userRepository.save(any())).thenReturn(loggedUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(contactUser));

        Set<UserDto> result = userService.addToContactList(2L);
        UserDto resultUser = result.iterator().next();

        assertEquals(1, result.size());
        assertEquals("contact", resultUser.getUsername());
        verify(userRepository).save(loggedUser);
    }

    @Test
    void addToContactList_ThrowsException_WhenUserIsContactListOwner() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setId(1L);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));

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
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        User contactUser = new User("contact", "password", "name", "surname");
        loggedUser.setId(1L);
        contactUser.setId(2L);
        loggedUser.setContactList(new HashSet<>(Set.of(2L, 3L)));

        when(userRepository.existsById(3L)).thenReturn(true);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));
        when(userRepository.save(loggedUser)).thenReturn(loggedUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(contactUser));

        Set<UserDto> result = userService.deleteFromContactList(3L);
        UserDto resultUser = result.iterator().next();

        assertEquals(1, result.size());
        assertEquals("contact", resultUser.getUsername());
    }

    @Test
    void deleteFromContactList_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.addToContactList(1L));
    }

    @Test
    void deleteFromContactList_ThrowsException_WhenUserIsNotOnContactList() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setContactList(new HashSet<>(Set.of(2L, 3L)));
        when(userRepository.existsById(4L)).thenReturn(true);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));

        assertThrows(InvalidRequestException.class, () -> userService.deleteFromContactList(4L));
    }

    @Test
    void getMyContactList_ReturnsContactList_WhenUserHasContactList() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        User user2 = new User("username2", "password2", "firstName2", "surname2");
        loggedUser.setId(1L);
        user2.setId(2L);
        loggedUser.setContactList(new HashSet<>(Set.of(2L)));

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        Set<UserDto> result = userService.getMyContactList();
        UserDto resultUser1 = result.iterator().next();

        assertEquals(1, result.size());
        assertEquals("username2", resultUser1.getUsername());
    }

    @Test
    void getMyContactList_ThrowsException_WhenListIsEmpty() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User loggedUser = new User("username", "password", "firstName", "surname");
        loggedUser.setContactList(new HashSet<>());

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(loggedUser));

        assertThrows(EmptyListException.class, () -> userService.getMyContactList());
    }
}