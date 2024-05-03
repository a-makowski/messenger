package com.makowski.messenger.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatDto {
    private Long chatId;
    private Set<UserDto> members;        
}
