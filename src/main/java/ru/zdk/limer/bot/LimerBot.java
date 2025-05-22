package ru.zdk.limer.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.List;

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String url = update.getMessage().getText();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");

            WebDriver driver = new ChromeDriver(options);
            try {
                driver.get(url);

                // Получаем содержимое всей страницы
                String pageSource = driver.getPageSource();
                logger.info(pageSource);

                // Извлечь размеры
                List<WebElement> sizeElements = driver.findElements(By.cssSelector(".ProductSizeSelector__item"));
                List<String> availableSizes = new ArrayList<>();
                List<String> unavailableSizes = new ArrayList<>();

                for (WebElement sizeElement : sizeElements) {
                    String sizeText = sizeElement.getText().trim();
                    if (sizeElement.getAttribute("class").contains("_disabled")) {
                        unavailableSizes.add(sizeText);
                    } else {
                        availableSizes.add(sizeText);
                    }
                }

                // Собираем строку с результатом
                StringBuilder sizesMessage = new StringBuilder("Размеры на странице:\n");
                sizesMessage.append("Доступные: ");
                sizesMessage.append(String.join(", ", availableSizes));
                sizesMessage.append("\nНедоступные: ");
                sizesMessage.append(String.join(", ", unavailableSizes));

                // Также получить JSON, если нужно
                List<WebElement> scripts = driver.findElements(By.cssSelector("script[type='application/ld+json']"));
                if (!scripts.isEmpty()) {
                    String jsonText = scripts.getFirst().getAttribute("innerHTML");
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(jsonText);

                    String title = rootNode.path("name").asText(null);
                    String description = rootNode.path("description").asText(null);

                    sizesMessage.append("\n\nДанные из JSON:\n");
                    if (title != null) sizesMessage.append("Название: ").append(title).append("\n");
                    if (description != null) sizesMessage.append("Описание: ").append(description).append("\n");
                }

                execute(new SendMessage(chatId, sizesMessage.toString()));

            } catch (Exception e) {
                logger.error("Ошибка при загрузке страницы или парсинге JSON: " + e.getMessage(), e);
                try {
                    execute(new SendMessage(chatId, "Ошибка при обработке ссылки: " + e.getMessage()));
                } catch (TelegramApiException ex) {
                    logger.error("Ошибка при отправке сообщения об ошибке: " + ex.getMessage());
                }
            } finally {
                driver.quit();
            }
        }
    }
}
