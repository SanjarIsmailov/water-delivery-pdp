package com.example.demo.repo;

import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.enums.TelegramState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, UUID> {

  Optional<TelegramUser> findByChatId(Long chatId);

  List<TelegramUser> findAllByActiveIsFalseAndState(TelegramState telegramState);

  Optional<TelegramUser> findByPhone(String phoneNumber);

}