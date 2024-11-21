package com.makowski.messenger.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.makowski.messenger.entity.Message;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {

    Page<Message> findByPermanentFalseAndDateTimeBefore(LocalDateTime expireDate, Pageable pageable);
}