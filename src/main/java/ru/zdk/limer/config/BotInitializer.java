package ru.zdk.limer.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.zdk.limer.bot.LimerBot;

@Component
public class BotInitializer {
    private final LimerBot limerBot;

    public BotInitializer(LimerBot limerBot) {
        this.limerBot = limerBot;
    }

    @PostConstruct
    public void registerBot() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(limerBot);
    }
}