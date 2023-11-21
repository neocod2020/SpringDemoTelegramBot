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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
    final static String REGISTER_QUESTION = "Do you really want to register?";
    final static String YES_BUTTON = "YES_BUTTON";
    final static String NO_BUTTON = "NO_BUTTON";
    final static String NO_RECOGNIZED_COMMAND = "Sorry, can't understand your command";
    final static String ERROR_MSG = "Error occurred: ";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a greeting answer"));
        listOfCommands.add(new BotCommand("/register", "start a register procedure"));
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

            // organization of sending to all the users the text after command /send and a space after it
            if (messageText.contains("/send") && config.getOwnerId() == chatId) {
                String textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                Iterable<User> users = userService.findAll();
                for (User user : users) {
                    prepareAndSendMessage(user.getChatId(), textToSend);
                }
            } else {
                switch (messageText) {
                    case "/start" -> {
                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    }
                    case "/help" ->
                        prepareAndSendMessage(chatId, HELP_TEXT);
                    case "/register" -> {
                        register(update.getMessage().getChatId());
                    }

                    default ->
                        prepareAndSendMessage(chatId, NO_RECOGNIZED_COMMAND);
                }
            }
        } else if (update.hasCallbackQuery()) {
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            EditMessageText message = new EditMessageText();

            if (callbackData.equals(YES_BUTTON)) {
                String text = "You press YES button";
                executeEditMessageText(chatId, messageId, text);

            } else if (callbackData.equals(NO_BUTTON)) {
                String text = "You press NO button";
                executeEditMessageText(chatId, messageId, text);
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        // String answer = "Hi, " + firstName + ", nice to meet you!";
        String answer = EmojiParser.parseToUnicode("Hi, " + firstName + ", nice to meet you! " + " :blush:");
        log.info("Replied to user " + firstName);
        prepareAndSendMessage(chatId, answer);
    }

    private void prepareAndSendMessage(long chatId, String answer) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(answer);

        setMarkup(message);

        executeMessage(message);
    }

    private void registerUser(Message message) {
        if (userService.findById(message.getChatId()).isEmpty()) {
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

    private void setMarkup(SendMessage msg) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        keyboardRows.add(row);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("register");
        row1.add("check my data");
        row1.add("delete my data");
        keyboardRows.add(row1);

        keyboardMarkup.setKeyboard(keyboardRows);
        msg.setReplyMarkup(keyboardMarkup);
    }

    private void register(long chatId) {

        SendMessage registerMessage = new SendMessage();
        registerMessage.setChatId(String.valueOf(chatId));
        registerMessage.setText(REGISTER_QUESTION);

        // create two buttons - YES and NO in line
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);

        registerMessage.setReplyMarkup(markupInLine);

        executeMessage(registerMessage);
    }

    private void executeEditMessageText(long chatId, long messageId, String text) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId((int) messageId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException ex) {
            log.error(ERROR_MSG + ex.getMessage());
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException ex) {
            log.error(ERROR_MSG + ex.getMessage());
        }
    }
}
