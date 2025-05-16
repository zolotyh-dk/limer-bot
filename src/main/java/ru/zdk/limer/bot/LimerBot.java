package ru.zdk.limer.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.zdk.limer.config.BotProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class LimerBot extends TelegramLongPollingBot {
    private final BotProperties botProperties;

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        try {
            execute(new SendMessage(chatId, "Привет! Это " + botProperties.getName()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
