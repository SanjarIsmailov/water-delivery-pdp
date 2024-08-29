package com.example.demo.bot;

import com.example.demo.entity.Courier;
import com.example.demo.entity.DeliveryTime;
import com.example.demo.entity.Order;
import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.enums.BottleTypes;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.entity.enums.TelegramState;
import com.example.demo.repo.CourierRepository;
import com.example.demo.repo.DeliveryTimeRepository;
import com.example.demo.repo.OrderRepository;
import com.example.demo.repo.TelegramUserRepository;
import com.example.demo.utils.DistrictUtil;
import com.example.demo.utils.PhoneUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BotService {

    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private final DistrictUtil districtUtil;
    private final DeliveryTimeRepository deliveryTimeRepository;
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;


    public TelegramUser getOrCreateUser(Long id) {
        Optional<TelegramUser> byChatId =
                telegramUserRepository.findByChatId(id);
        if (byChatId.isPresent()) {
            return byChatId.get();
        } else {
            TelegramUser telegramUser = TelegramUser.builder()
                    .chatId(id)
                    .build();
            telegramUserRepository.save(telegramUser);
            return telegramUser;
        }
    }

    public void acceptStartAskForContact(TelegramUser tgUser) {
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), BotConstant.SHARE_CONTACT_MSG);
        sendMessage.replyMarkup(generateContactButton());
        telegramBot.execute(sendMessage);
        tgUser.setState(TelegramState.SHARING_CONTACT);
        telegramUserRepository.save(tgUser);
    }

    private Keyboard generateContactButton() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(BotConstant.SHARE_CONTACT_BTN)
                        .requestContact(true)
        ).resizeKeyboard(true);
    }

    private Keyboard generateLocationButton() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(BotConstant.SHARE_LOCATION_BTN)
                        .requestLocation(true)
        ).resizeKeyboard(true);
    }


    public void acceptPhoneAskForLocation(String phone, TelegramUser tgUser) {
        String userPhone = PhoneUtil.repairPhone(phone);
        tgUser.setPhone(userPhone);
        telegramUserRepository.save(tgUser);

        Optional<Courier> courierOpt = Optional.ofNullable(courierRepository.findByPhoneNumber(userPhone));
        if (courierOpt.isPresent()) {
            tgUser.setState(TelegramState.COURIER);
            telegramUserRepository.save(tgUser);
        } else {
            sendLocation(tgUser);
        }
    }


    private void sendLocation(TelegramUser tgUser) {
        SendMessage sendMessage = new SendMessage(
                tgUser.getChatId(),
                BotConstant.PLEASE_SHARE_LOCATION
        );
        sendMessage.replyMarkup(generateLocationButton());
        telegramBot.execute(sendMessage);
        tgUser.setState(TelegramState.SHARING_LOCATION);
        telegramUserRepository.save(tgUser);
    }

    public void acceptLocationAskToWait(Location location, TelegramUser tgUser) {
        tgUser.setLocation(new com.example.demo.entity.Location(location.longitude(), location.latitude()));
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), BotConstant.PLEASE_WAIT);
        telegramBot.execute(sendMessage);
        tgUser.setState(TelegramState.WAITING);
        telegramUserRepository.save(tgUser);
    }

    public void openCabinetForUser(TelegramUser telegramUser) {
        SendMessage sendMessage1 = new SendMessage(telegramUser.getChatId(), BotConstant.LOGIN_SUCCESS);
        sendMessage1.replyMarkup(new ReplyKeyboardRemove());
        telegramBot.execute(sendMessage1);

        SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), BotConstant.YOUR_CABINET);
        sendMessage.replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton(BotConstant.ORDER_WATER_MSG)
                        .callbackData(BotConstant.ORDER_WATER)
        ));
        telegramBot.execute(sendMessage);
        telegramUser.setState(TelegramState.CABINET);
        telegramUserRepository.save(telegramUser);
    }

    public void beforeKill(TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(telegramUser.getChatId(),
                BotConstant.KILL_MESSAGE
        );
        telegramBot.execute(sendMessage);
    }

    public void startOrderingAskForBottleType(TelegramUser tgUser) {
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), BotConstant.SELECT_BOTTLE_TYPE);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton[] buttons = Arrays.stream(BottleTypes.values()).map(item -> new InlineKeyboardButton(
                item.getType()
        ).callbackData(item.name())).toArray(InlineKeyboardButton[]::new);
        inlineKeyboardMarkup.addRow(buttons);
        sendMessage.replyMarkup(inlineKeyboardMarkup);
        tgUser.setState(TelegramState.SELECTING_BOTTLE_TYPES);
        telegramUserRepository.save(tgUser);
        telegramBot.execute(sendMessage);

    }

    public void acceptBottleTypeAskForAmount(CallbackQuery callbackQuery, TelegramUser tgUser) {
        String data = callbackQuery.data();
        BottleTypes bottleTypes = BottleTypes.valueOf(data);
        tgUser.setBottleTypes(bottleTypes);
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), "Sizga nechta baklashka kerak:");
        sendMessage.replyMarkup(generateBottleCounterBtns(tgUser));
        tgUser.setState(TelegramState.ENTERING_BOTTLES_AMOUNT);
        SendResponse res = telegramBot.execute(sendMessage);
        tgUser.setEditingMessageId(res.message().messageId());
        telegramUserRepository.save(tgUser);
    }

    private InlineKeyboardMarkup generateBottleCounterBtns(TelegramUser tgUser) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("-").callbackData(BotConstant.MINUS),
                new InlineKeyboardButton(tgUser.getBottleCount() + "").callbackData("###"),
                new InlineKeyboardButton("+").callbackData(BotConstant.PLUS));
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Tasdiqlash").callbackData(BotConstant.ACCEPT_COUNT)
        );
        return inlineKeyboardMarkup;
    }

    private void tomorrow(List<DeliveryTime> allTimes, TelegramUser tgUser) {
        InlineKeyboardButton[] buttons = allTimes.stream().map(item ->
                new InlineKeyboardButton(item.toString())
                        .callbackData("TOMORROW/" + item.getId())
        ).toArray(InlineKeyboardButton[]::new);
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), "Ertaga:");
        sendMessage.replyMarkup(new InlineKeyboardMarkup(buttons));
        telegramBot.execute(sendMessage);
    }

    private void today(List<DeliveryTime> allTimes, TelegramUser tgUser) {
        InlineKeyboardButton[] buttons = allTimes.stream().filter(item -> LocalTime.now().plusHours(2).isBefore(item.getEnd())).map(item ->
                new InlineKeyboardButton(item.toString())
                        .callbackData("TODAY/" + item.getId())
        ).toArray(InlineKeyboardButton[]::new);
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), "Bugun:");
        sendMessage.replyMarkup(new InlineKeyboardMarkup(buttons));
        telegramBot.execute(sendMessage);
    }

    public void changeCount(String operation, TelegramUser tgUser) {
        if (operation.equals("+")) {
            tgUser.setBottleCount(tgUser.getBottleCount() + 2);
        } else {
            if (tgUser.getBottleCount() != 2) {
                tgUser.setBottleCount(tgUser.getBottleCount() - 2);
            }
        }
        telegramUserRepository.save(tgUser);
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(
                tgUser.getChatId(), tgUser.getEditingMessageId()
        );
        editMessageReplyMarkup.replyMarkup(generateBottleCounterBtns(tgUser));
        telegramBot.execute(editMessageReplyMarkup);
    }

    public void acceptBottleCountAskForTime(TelegramUser tgUser) {
        tgUser.setState(TelegramState.SELECTING_TIME);
        telegramUserRepository.save(tgUser);
        List<DeliveryTime> allTimes = deliveryTimeRepository.findAll();
        today(allTimes, tgUser);
        tomorrow(allTimes, tgUser);
    }

    public void acceptTimeAndCreateOrder(CallbackQuery callbackQuery, TelegramUser tgUser) {
        String day = callbackQuery.data().split("/")[0];
        Integer deliveryTimeId = Integer.parseInt(callbackQuery.data().split("/")[1]);
        DeliveryTime deliveryTime = deliveryTimeRepository.findById(deliveryTimeId).orElseThrow(() -> new RuntimeException("bunday vaqt yuq"));
        LocalDateTime startTime = LocalDateTime.of(getDate(day), deliveryTime.getStart());
        LocalDateTime endTime = LocalDateTime.of(getDate(day), deliveryTime.getEnd());
        Order order = Order.builder()
                .orderStatus(OrderStatus.NEW)
                .bottleCount(tgUser.getBottleCount())
                .bottleTypes(tgUser.getBottleTypes())
                .telegramUserId(tgUser.getId())
                .startTime(startTime)
                .endTime(endTime)
                .phone(tgUser.getPhone())
                .location(tgUser.getLocation())
                .district(tgUser.getDistrict())
                .build();
        orderRepository.save(order);
        tgUser.setState(TelegramState.ACCEPT_ORDER);
        tgUser.setLastOrderId(order.getId());
        telegramUserRepository.save(tgUser);
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), orderMessageForAccept(order));
        sendMessage.replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton(BotConstant.ACCEPT)
                        .callbackData(BotConstant.ACCEPT_ORDER),
                new InlineKeyboardButton(BotConstant.CANCEL)
                        .callbackData(BotConstant.CANCEL_ORDER)
        ));
        telegramBot.execute(sendMessage);
    }

    private String orderMessageForAccept(Order order) {
        int totalSum = order.getBottleTypes().getPrice() * order.getBottleCount();
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE);
        LocalDate localDate = order.getStartTime().toLocalDate();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy dd-MMMM", Locale.forLanguageTag("uz"));
        return """
                Buyurtmangiz:
                %s dan %d dona
                Jami: %s so'm
                Yetkazilish vaqti:
                %s kuni %s dan %s gacha
                Iltimos yetkazilish vaqtida uyda bo'lishni unutmang
                """.formatted(
                order.getBottleTypes().getType(),
                order.getBottleCount(),
                numberFormat.format(totalSum),
                localDate.format(dateFormatter),
                order.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                order.getEndTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }

    public LocalDate getDate(String day) {
        if (day.equals("TODAY")) {
            return LocalDate.now();
        } else {
            return LocalDate.now().plusDays(1);
        }
    }

    public void cancelOrderAndGoToCabinet(TelegramUser tgUser) {
        orderRepository.deleteById(tgUser.getLastOrderId());
        openCabinetForUser(tgUser);
    }

    public void acceptOrderAndWait(TelegramUser tgUser) {
        Order order = orderRepository.findById(tgUser.getLastOrderId()).orElseThrow(() -> new RuntimeException("order not found"));
        order.setOrderStatus(OrderStatus.ACCEPTED);
        orderRepository.save(order);
        tgUser.setState(TelegramState.WAITING);
        telegramUserRepository.save(tgUser);
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(),
                "Buyurtmangiz qabul qilindi. Iltimos belgilangan vaqtda manzilingizda bo'lishni unutmang"
                );
        telegramBot.execute(sendMessage);
    }

    public void acceptCourierStartDelivery(TelegramUser tgUser) {
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), "Siz kuryersiz, bu yerdan buyurtmalaringizni ko'rishingiz mumkin:");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
                new InlineKeyboardButton("Mening buyurtmalarim")
                        .callbackData("MY_ORDERS")
        );
        telegramUserRepository.save(tgUser);
        sendMessage.replyMarkup(inlineKeyboardMarkup);
        telegramBot.execute(sendMessage);
    }

    public void showOrders(TelegramUser tgUser) {
        // Fetch orders
        List<Order> orders = orderRepository.findAll().stream().filter(item -> item.getCourier().getId().equals(tgUser.getId())).toList();

        // Create inline keyboard buttons
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Order order : orders) {
            InlineKeyboardButton button = new InlineKeyboardButton(order.getDistrict().getName() + " " + order.getBottleCount())
                    .callbackData(order.getId().toString()); // Set the callback data
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);

            keyboard.add(row);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup((InlineKeyboardButton) keyboard);

        // Create and send message with inline keyboard
        SendMessage message = new SendMessage(tgUser.getChatId(), "Here are your orders:")
                .replyMarkup(markup);

        SendResponse response = telegramBot.execute(message);

        if (!response.isOk()) {
            // Handle errors here
            System.err.println("Error sending message: " + response.description());
        }
    }

    public DistrictUtil getDistrictUtil() {
        return districtUtil;
    }
}
