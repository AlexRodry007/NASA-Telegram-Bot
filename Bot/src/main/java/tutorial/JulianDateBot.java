package tutorial;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JulianDateBot extends TelegramLongPollingBot {

    private boolean screaming = false;
    String msgtype = "text";
    String msgsubtype = "all";

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();



        if(msg.isCommand()){
            if(msg.getText().equals("/scream"))         //If the command was /scream, we switch gears
                screaming = true;
            else if (msg.getText().equals("/whisper"))  //Otherwise, we return to normal
                screaming = false;

            if(msg.getText().equals("/test"))
            {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://ssd-api.jpl.nasa.gov/jd_cal.api"))
                        .header("Content-Type", "application/json")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = null;
                try {
                    response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(response.body());
            }
            if(msg.getText().equals("/jd"))
            {
                msgtype="jd";
            }
            if(msg.getText().equals("/text"))
            {
                msgtype="text";
            }
            if(msg.getText().equals("/cd"))
            {
                msgtype="cd";
            }
            if(msg.getText().equals("/all"))
            {
                msgsubtype="all";
            }
            if(msg.getText().equals("/year"))
            {
                msgsubtype="year";
            }
            if(msg.getText().equals("/month"))
            {
                msgsubtype="month";
            }
            if(msg.getText().equals("/dow"))
            {
                msgsubtype="dow";
            }
            if(msg.getText().equals("/doy"))
            {
                msgsubtype="doy";
            }
            return;                                     //We don't want to echo commands, so we exit
        }

        JSONObject myObject = null;
        HttpRequest request = null;
        HttpResponse<String> response = null;
        switch (msgtype) {
            case "text":
                if(screaming)                            //If we are screaming
                    scream(id, update.getMessage());     //Call a custom method
                else
                    copyMessage(id, msg.getMessageId());
                return;

            case "jd":
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://ssd-api.jpl.nasa.gov/jd_cal.api?jd="+msg.getText()))
                        .header("Content-Type", "application/json")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                try {
                    response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(response.body());
                myObject = new JSONObject(response.body());

                break;
            case "cd":
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://ssd-api.jpl.nasa.gov/jd_cal.api?cd="+msg.getText()))
                        .header("Content-Type", "application/json")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                try {
                    response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(response.body());
                myObject = new JSONObject(response.body());

                break;

        }
        switch (msgsubtype){
            case("all"):
                if(msgtype.equals("cd"))
                {
                    String jd = myObject.getString("jd");
                    sendText(id, jd);
                }
                else
                {
                    String cd = myObject.getString("cd");
                    sendText(id, cd);
                }
                break;
            case("year"):
                String year = String.valueOf(myObject.getInt("year"));
                sendText(id, year);
                break;
            case("month"):
                String month_name = myObject.getString("month_name");
                sendText(id, month_name);
                break;
            case("doy"):
                String doy = String.valueOf(myObject.getInt("doy"));
                sendText(id, doy);
                break;
            case("dow"):
                String dow = myObject.getString("dow_name");
                sendText(id, dow);
                break;
        }
        //Else proceed normally
    }

    private void scream(Long id, Message msg) {
        if(msg.hasText())
            sendText(id, msg.getText().toUpperCase());
        else
            copyMessage(id, msg.getMessageId());  //We can't really scream a sticker
         }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
    public void copyMessage(Long who, Integer msgId){
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())  //We copy from the user
                .chatId(who.toString())      //And send it back to him
                .messageId(msgId)            //Specifying what message
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "JulianDateBot";
    }

    @Override
    public String getBotToken() {
        return "5983273473:AAG0CZ9Opv6SInh3M5rB3NKKSIJMHbpnZlI";
    }
}

