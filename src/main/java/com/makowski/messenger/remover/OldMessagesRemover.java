package com.makowski.messenger.remover;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.makowski.messenger.entity.Message;
import com.makowski.messenger.repository.MessageRepository;
import com.makowski.messenger.service.ChatService;

@Component
public class OldMessagesRemover {

    private MessageRepository messageRepository;
    private ChatService chatService;

    @Scheduled(cron = "0 0 0 * * *")                                            
    public void deleteOldMessages() {
        for(Long i = 1L; i<= messageRepository.count(); i++) {
            Optional<Message> message = messageRepository.findById(i);
            if (message.isPresent()) {
                Message checkMessage = message.get();
                if (!checkMessage.isPermanent()) {
                    LocalDate expireDate = checkMessage.getDateTime().toLocalDate().plusDays(7);
                    if(expireDate.isBefore(LocalDate.now())) {
                        Long chatId = checkMessage.getChat().getId();
                        messageRepository.deleteById(i);
                        chatService.deleteChatIfEmpty(chatId);
                    }
                }
            }
        }
    }
}
