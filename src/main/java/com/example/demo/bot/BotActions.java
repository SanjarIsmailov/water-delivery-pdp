package com.example.demo.bot;

import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.enums.TelegramState;
import com.example.demo.repo.CourierRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BotActions {

    private final BotService botService;
    private final CourierRepository courierRepository;
    private final TelegramBot telegramBot;

    @Async
    public void handle(Update update) {
        if (update.message() != null) {
            Message message = update.message();
            TelegramUser tgUser = botService.getOrCreateUser(message.chat().id());
            if (message.text() != null) {
                String text = message.text();
                if (text.equals("/start")) {
                    if (tgUser.isActive()) {
                        botService.openCabinetForUser(tgUser);
                    } else {
                        botService.acceptStartAskForContact(tgUser);
                    }
                } else if (tgUser.getState().equals(TelegramState.SHARING_CONTACT)) {
                        botService.acceptPhoneAskForLocation(text, tgUser);
                }
            } else if (message.contact() != null) {
                if (tgUser.getState().equals(TelegramState.SHARING_CONTACT)) {
                    botService.acceptPhoneAskForLocation(message.contact().phoneNumber(), tgUser);
                }
            } else if (message.location() != null) {
                botService.acceptLocationAskToWait(message.location(), tgUser);
            }
        } else if (update.callbackQuery() != null) {
            CallbackQuery callbackQuery = update.callbackQuery();
            TelegramUser tgUser = botService.getOrCreateUser(callbackQuery.from().id());
            if (tgUser.getState().equals(TelegramState.CABINET)) {
                if (callbackQuery.data().equals(BotConstant.ORDER_WATER)) {
                    botService.startOrderingAskForBottleType(tgUser);
                }
            } else if (tgUser.getState().equals(TelegramState.SELECTING_BOTTLE_TYPES)) {
                botService.acceptBottleTypeAskForAmount(callbackQuery, tgUser);
            } else if (tgUser.getState().equals(TelegramState.ENTERING_BOTTLES_AMOUNT)) {
                if (callbackQuery.data().equals(BotConstant.PLUS)) {
                    botService.changeCount("+", tgUser);
                } else if (callbackQuery.data().equals(BotConstant.MINUS)) {
                    botService.changeCount("-", tgUser);
                } else if (callbackQuery.data().equals(BotConstant.ACCEPT_COUNT)) {
                    botService.acceptBottleCountAskForTime(tgUser);
                }
            } else if (tgUser.getState().equals(TelegramState.SELECTING_TIME)) {
                botService.acceptTimeAndCreateOrder(callbackQuery, tgUser);
            } else if (tgUser.getState().equals(TelegramState.ACCEPT_ORDER)) {
                if (callbackQuery.data().equals(BotConstant.ACCEPT_ORDER)) {
                    botService.acceptOrderAndWait(tgUser);
                } else if (callbackQuery.data().equals(BotConstant.CANCEL_ORDER)) {
                    botService.cancelOrderAndGoToCabinet(tgUser);
                }
            } else if (tgUser.getState().equals(TelegramState.COURIER)) {
                if (callbackQuery.data().equals("MY_ORDERS")){
                    botService.showOrders(tgUser);
                }
            }
        }
    }
}
