package com.makowski.messenger.entity;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.makowski.messenger.constants.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@Table(name = "message")
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;
                                          
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @NonNull
    @Column(name = "receivers", nullable = false)
    private Set<Long> receiverId;

    @Column(name = "permanent")
    private boolean permanent = false;

    @JsonFormat(pattern = Constants.FORMATTER)              
    @Column(name = "date_time")
    private LocalDateTime dateTime = LocalDateTime.now();

    @NotBlank(message = "Message cannot be empty")
    @NonNull
    @Column(name = "content", nullable = false)
    private String content;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    private Chat chat;
}