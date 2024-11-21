package com.makowski.messenger.remover;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.makowski.messenger.entity.Message;
import com.makowski.messenger.repository.MessageRepository;
import com.makowski.messenger.service.MessageService;

@Component
public class OldMessagesRemover {

    private static final int PAGE_SIZE = 100;
    private MessageRepository messageRepository;
    private MessageService messageService;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldMessages() {
        LocalDateTime expireDate = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Page<Message> page;

        do {
            page = messageRepository.findByPermanentFalseAndDateTimeBefore(expireDate, pageable);
            for (Message message : page.getContent()) {
                messageService.deleteMessage(message.getId());
            }
            pageable = page.nextPageable();
        } while (page.hasNext());
    }
}