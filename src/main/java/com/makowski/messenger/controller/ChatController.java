package com.makowski.messenger.controller;

import java.util.List;

import com.makowski.messenger.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.makowski.messenger.dto.ChatDto;
import com.makowski.messenger.entity.Chat;
import com.makowski.messenger.service.ChatService;

import lombok.AllArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@AllArgsConstructor
@Tag(name = "Chat Controller", description = "Chat entity manager")
@RequestMapping("/chat")
public class ChatController {
    
    private ChatService chatService;

    @Operation(summary = "Get my chats", description = "Returns a list of chats from a currently logged in user. List contains only members names and chat IDs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of a chat list", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChatDto.class)))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chat list is empty", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<List<ChatDto>> getMyChats() {
        return new ResponseEntity<>(chatService.getMyChats(), HttpStatus.OK);
    }

    @Operation(summary = "Get chat", description = "Returns a chat based on an ID. Allowed only for chat members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of a Chat", content = @Content(schema = @Schema(implementation = Chat.class))),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - allowed only for chat members", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chat with a selected ID doesn't exist in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{chatId}")
    public ResponseEntity<Chat> getChat(@PathVariable Long chatId) {
        return new ResponseEntity<>(chatService.getMyChat(chatId), HttpStatus.OK);
    }

    @Operation(summary = "Delete chat", description = "Delete chat with a selected ID. Allowed only for chat members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Chat successfully deleted from a database"),
            @ApiResponse(responseCode = "401", description = "JWT Token not valid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - allowed only for chat members", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chat doesn't exist in a database", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{chatId}")
    public ResponseEntity<HttpStatus> deleteChat(@PathVariable Long chatId) {
        chatService.deleteChatIfExist(chatId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
