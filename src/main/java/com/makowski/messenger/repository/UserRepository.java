package com.makowski.messenger.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.makowski.messenger.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

    public Optional<User> findByUsername(String username);
}
