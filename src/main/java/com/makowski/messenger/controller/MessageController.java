package com.makowski.messenger.controller;

import com.makowski.messenger.exception.ErrorResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.makowski.messenger.entity.Message;
import com.makowski.messenger.service.MessageService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@AllArgsConstructor
@Tag(name = "Message Controller", description = "Message entity manager")
@RequestMapping("/message")
public class MessageController {
    
    private MessageService messageService;

    @Operation(summary = "Save message", description = "Add a new message to database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful creation of a new message", content = @Content(schema = @Schema(implementation = Message.class))),
            @ApiResponse(responseCode = "400", description = "Invalid Request - Message is too long or empty or have no receivers (others than sender)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "One of receivers does not exist in a database ", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Message> saveMessage(@Valid @RequestBody Message message) {
        return new ResponseEntity<>(messageService.saveMessage(message), HttpStatus.CREATED);
    }

    @Operation(summary = "Update message", description = "Change content of a message with a selected ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message successfully updated", content = @Content(schema = @Schema(implementation = Message.class))),
            @ApiResponse(responseCode = "400", description = "Invalid Request - message is too long or empty", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Message can be updated only by its author", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Message doesn't exist in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@RequestBody String content, @PathVariable Long id) {
        return new ResponseEntity<>(messageService.updateMessage(content, id), HttpStatus.OK);                                
    }

    @Operation(summary = "Change flag", description = "Change flag of a message with a selected ID to the opposite. Allowed only for an owner of a message. Messages with a flag will NOT be automatically removed after 7 days.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flag successfully changed", content = @Content(schema = @Schema(implementation = Message.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "The message flag can only be changed by its author", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Message doesn't exist in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/flag/{id}")
    public ResponseEntity<Message> changeFlag(@PathVariable Long id) {
        return new ResponseEntity<>(messageService.changeFlag(id), HttpStatus.OK);
    }

    @Operation(summary = "Delete message", description = "Delete message with a selected ID from database. Allowed only for an owner of a message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Message successfully deleted from a database"),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Message can only be deleted by its author", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Message doesn't exist in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteMyMessage(@PathVariable Long id) {
        messageService.deleteMyMessage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
