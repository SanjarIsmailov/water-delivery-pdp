package com.example.demo.service;

import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.enums.TelegramState;
import com.example.demo.repo.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final TelegramUserRepository telegramUserRepository;

    public List<TelegramUser> getNotAcceptedUsers(){
        return telegramUserRepository.findAllByActiveIsFalseAndState(TelegramState.WAITING);
    }


}
