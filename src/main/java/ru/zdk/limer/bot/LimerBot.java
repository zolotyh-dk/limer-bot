package ru.zdk.limer.bot;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String url = update.getMessage().getText();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            WebDriver driver = new ChromeDriver(options);
            try {
                driver.get(url);
                logger.info(driver.getPageSource());

                // Поиск мета-тега og:title
                WebElement metaOgTitle = driver.findElement(By.cssSelector("meta[property='og:title']"));

                // Получение значения атрибута content
                String ogTitle = metaOgTitle.getAttribute("content");
                try {
                    execute(new SendMessage(chatId, "Вы запросили: " + ogTitle));
                } catch (TelegramApiException e) {
                    logger.error(e.getMessage());
                }
            } finally {
                driver.quit();
            }

        }
    }
}
