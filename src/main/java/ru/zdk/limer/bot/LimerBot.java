package ru.zdk.limer.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.zdk.limer.config.BotProperties;

@Component
public class LimerBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(LimerBot.class);

    private final BotProperties botProperties;

    public LimerBot(BotProperties botProperties) {
        this.botProperties = botProperties;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        try {
            execute(new SendMessage(chatId, "Привет! Это " + botProperties.getName()));
        } catch (TelegramApiException e) {
            logger.error(e.getMessage());
        }
    }
}
