package com.makowski.messenger.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.makowski.messenger.entity.Message;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {

    Message save(Optional<Message> updatedMessage);
}