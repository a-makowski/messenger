package com.makowski.messenger.repository;

import org.springframework.data.repository.CrudRepository;

import com.makowski.messenger.entity.Chat;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends CrudRepository<Chat, Long> {

}