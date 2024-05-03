package com.makowski.messenger.controller;

import java.util.Set;

import com.makowski.messenger.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.makowski.messenger.dto.PasswordDto;
import com.makowski.messenger.dto.UserDto;
import com.makowski.messenger.entity.User;
import com.makowski.messenger.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@Tag(name = "User Controller", description = "User entity manager")

@RequestMapping("/user")
public class UserController {
    
    private UserService userService;

    @Operation(summary = "Save user", description = "Add a new user to database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful creation of a user", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid Request - Request body must contain valid user data. Username must be unique", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> saveUser(@Valid @RequestBody User user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }


    @Operation(summary = "Update user", description = "Allows to change first name and surname of a currently logged in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful update of a user", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Request body is not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.updateUser(userDto), HttpStatus.OK);
    }

    @Operation(summary = "Change password", description = "Change password from a currently logged in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful change of password"),
            @ApiResponse(responseCode = "400", description = "Invalid Request - a new password and repeated new password don't match", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current password is incorrect", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PutMapping("/password")
    public ResponseEntity<HttpStatus> changePassword(@Valid @RequestBody PasswordDto password) {
        userService.changePassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Delete user", description = "Delete a currently logged in user from database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted from a database"),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteUser() {
        userService.deleteUser();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get user", description = "Returns username, first name and surname from a user with a selected ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of a user", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User with a selected ID doesn't exist in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return new ResponseEntity<>(userService.getUserDto(id), HttpStatus.OK);
    }

    @Operation(summary = "Find user", description = "Returns a list of users whose name (user-, first- and surname) contains the specified phrase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of a list", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid Request - request body must contain searched phrase", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "There's no such user in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Set<UserDto>> findUser(@RequestBody String phrase) {
        return new ResponseEntity<>(userService.findUser(phrase), HttpStatus.OK);
    }

    @Operation(summary = "Add user to my contact list", description = "Add a new contact to a contact list of a currently logged in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A new contact successfully added to a contact list", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid Request - user cannot add himself to the contact list", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User with a selected ID doesn't exist in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/contacts/add/{contactId}")
    public ResponseEntity<Set<UserDto>> addToContactList(@PathVariable Long contactId) {
        return new ResponseEntity<>(userService.addToContactList(contactId), HttpStatus.OK);
    }

    @Operation(summary = "Delete user from my contact list", description = "Delete a contact with a selected ID from a contact list of a currently logged in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact successfully removed from a contact list", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid Request - there is no contact with a selected ID on a contact list", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "There is no contact with a selected ID in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/contacts/delete/{contactId}")
    public ResponseEntity<Set<UserDto>> deleteFromContactList(@PathVariable Long contactId) {
        return new ResponseEntity<>(userService.deleteFromContactList(contactId), HttpStatus.OK);
    }

    @Operation(summary = "Get my contact list", description = "Returns a contact list from a currently logged in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of a contact list", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "There is no contact on a list", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/contacts")
    public ResponseEntity<Set<UserDto>> getMyContactList() {
        return new ResponseEntity<>(userService.getMyContactList(), HttpStatus.OK);
    }
}
