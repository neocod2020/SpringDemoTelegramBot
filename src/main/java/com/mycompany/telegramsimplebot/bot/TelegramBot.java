package com.mycompany.telegramsimplebot.bot;

import com.mycompany.telegramsimplebot.config.BotConfig;
import com.mycompany.telegramsimplebot.entity.User;
import com.mycompany.telegramsimplebot.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    UserService userService;

    final static String HELP_TEXT = """
                                    This bot is created to demonstrate Spring capabilities.
                                    
                                    You can execute commands from the main menu on the left or by Type /start to see a welcome message 
                                    
                                    Type /mydata to see data stored about yourself
                                    
                                    Type /help to see this message again
                                    
                                    """;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a greeting answer"));
        listOfCommands.add(new BotCommand("/mydata", "get your data stored"));
        listOfCommands.add(new BotCommand("/deletedata", "delete your data"));
        listOfCommands.add(new BotCommand("/help", "info how to use this bot"));
        listOfCommands.add(new BotCommand("/settings", "set your preferences"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException ex) {
            log.error("Error setting bot's command list: " + ex.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> {
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                }
                case "/help" ->
                    sendMessage(chatId, HELP_TEXT);
                default ->
                    sendMessage(chatId, "Sorry, can't understand your command");
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        // String answer = "Hi, " + firstName + ", nice to meet you!";
        String answer = EmojiParser.parseToUnicode( "Hi, " + firstName + ", nice to meet you! " + " :blush:");
        log.info("Replied to user " + firstName);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String answer) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(answer);
        try {
            execute(message);
        } catch (TelegramApiException ex) {
            log.error("Error occurred: " + ex.getMessage());
        }
    }

    private void registerUser(Message message) {

        if (!userService.existsById(message.getChatId())) {
            Long chatId = message.getChatId();
            Chat chat = message.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userService.save(user);
            log.info("User saved: " + user);
        }
    }

}
