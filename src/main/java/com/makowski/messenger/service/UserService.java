package com.makowski.messenger.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.makowski.messenger.dto.ChatDto;
import com.makowski.messenger.dto.PasswordDto;
import com.makowski.messenger.dto.UserDto;
import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.entity.User;
import com.makowski.messenger.exception.EntityNotFoundException;
import com.makowski.messenger.exception.InvalidRequestException;
import com.makowski.messenger.exception.NoSuchUserException;
import com.makowski.messenger.exception.PasswordNotEqualsException;
import com.makowski.messenger.exception.AccessDeniedException;
import com.makowski.messenger.exception.EmptyListException;
import com.makowski.messenger.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
 
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    

    public UserDto createUser(User user) {
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername()))
            throw new InvalidRequestException(user.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setContactList(new HashSet<>());
        saveUser(user);
        return getUserDto(user.getId());
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) return user.get();
            else throw new EntityNotFoundException(username, User.class);
    }

    public User getLoggedUser() {
        return findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public UserDto updateUser(UserDto userDto) {
        User updatedUser = getLoggedUser();
        updatedUser.setFirstName(userDto.getFirstName());
        updatedUser.setSurname(userDto.getSurname());
        saveUser(updatedUser);
        return getUserDto(updatedUser.getId());
    }

    public void changePassword(PasswordDto password) {
        User user = getLoggedUser();
        if (bCryptPasswordEncoder.matches(password.getOldPassword(), user.getPassword())) {
            if (password.getNewPassword().equals(password.getRepeatNewPassword())) {
                user.setPassword(bCryptPasswordEncoder.encode(password.getNewPassword()));
                saveUser(user);
            } else throw new PasswordNotEqualsException();
        } else throw new AccessDeniedException();
    }

    public void deleteUser() {
        userRepository.deleteById(getLoggedUser().getId());
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(id, User.class));
    }

    public UserDto getUserDto(Long id) {
        User user = getUser(id);
        return new UserDto(id, user.getUsername(), user.getFirstName(), user.getSurname());
    }

    public List<ChatDto> getUsersChats() {             
        List<Chat> chats = getLoggedUser().getChats();
        if (chats.isEmpty()) {
            throw new EmptyListException("Chat");
        }                                                                   
        List<ChatDto> chatsDto = new ArrayList<>();
        for (Chat chat : chats) {
            Set<UserDto> members = new HashSet<>();
            for (User user : chat.getMembers()) {
                members.add(getUserDto(user.getId()));
            }
            chatsDto.add(new ChatDto(chat.getId(), members));
        }
        return chatsDto;
    }

    public Set<UserDto> findUser(String phrase) {
        if (phrase.isBlank() || phrase.isEmpty()) throw new InvalidRequestException("Search phrase cannot be empty");
        Set<UserDto> results = new HashSet<>();    
        for (User user : userRepository.findAll()) {
            String name = user.getUsername() + " " + user.getFirstName() + " " + user.getSurname();
            if (name.toUpperCase().contains(phrase.toUpperCase())) {
                results.add(getUserDto(user.getId()));
            }
        }
        if (results.isEmpty()) throw new NoSuchUserException();
            else return results;
    }

    public Set<UserDto> addToContactList(Long contactId) {
        if (userRepository.existsById(contactId)) {
            User user = getLoggedUser();
            if (!user.getId().equals(contactId)) {
                user.getContactList().add(contactId);
                saveUser(user);
            } else throw new InvalidRequestException("Owner of the list cannot be on the list");
        } else throw new EntityNotFoundException(contactId, User.class);
        return getMyContactList();
    }

    public Set<UserDto> deleteFromContactList(Long contactId) {
        if (userRepository.existsById(contactId)) {
            User user = getLoggedUser();
            if (user.getContactList().contains(contactId)) {
                user.getContactList().remove(contactId);
                saveUser(user);
                return getMyContactList();
            } else throw new InvalidRequestException("There is no such user on contact list");
        } else throw new EntityNotFoundException(contactId, User.class);
    }

    public Set<UserDto> getMyContactList() {       
        Set<Long> idList = getLoggedUser().getContactList();
        if (idList.isEmpty()) throw new EmptyListException("Contact");
        Set<UserDto> userList = new HashSet<>();
        for (Long id : idList) {
            userList.add(getUserDto(id));
        }
        return userList;
    }
}