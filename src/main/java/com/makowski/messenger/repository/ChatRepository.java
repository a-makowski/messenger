package com.makowski.messenger.repository;

import org.springframework.data.repository.CrudRepository;

import com.makowski.messenger.entity.Chat;

public interface ChatRepository extends CrudRepository<Chat, Long> {

    
}
        