package uz.pdp.waterdeliverypdp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.waterdeliverypdp.entity.TelegramUser;
import uz.pdp.waterdeliverypdp.entity.enums.TelegramState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, UUID> {
    Optional<TelegramUser> findByChatId(Long chatId);

    List<TelegramUser> findAllByActiveIsFalseAndState(TelegramState telegramState);

    Optional<TelegramUser> findByPhone(String phoneNumber);

}