package com.hivetech.calender.controller;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
//@RestController
public class CalendarController {

    public static final String ACCESS_TOKEN = "accessToken";
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @RequestMapping("/")
    public String index() {

        return "index";
    }

    @PostMapping("/update-access-token")
    public String updateAccessTokenToSession(String accessToken, HttpSession httpSession) {
        httpSession.setAttribute(ACCESS_TOKEN, accessToken);

        return "index";
    }

    @GetMapping("/list-event")
    public String displayListEvent(HttpSession httpSession, Model model) {
        String accessToken = (String) httpSession.getAttribute(ACCESS_TOKEN);

        if (!StringUtils.isEmpty(accessToken)) {
            try {
                model.addAttribute("events", getEvents(accessToken));
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "index";
    }

    private List<Event> getEvents(String accessToken) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        HttpRequestInitializer httpRequestInitializer = httpRequest -> {
            HttpHeaders headers = new HttpHeaders();

//            String accessToken = "ya29.Il_AB1N_1UVKVCz8ei5k15AK_-GDUYs5_uO82mtFtNtoIMj7MsCIzjfxQdFc1qdLvKtPGSOqK6viV54h-oxtZxYtbWQBCrHWMICO-ysp6ujX91JKq76WjqIedBAURCmDKQ";
            List<String> authorization = new ArrayList<>();
            authorization.add("Bearer " + accessToken);
            headers.set("Authorization", authorization);
            httpRequest.setHeaders(headers);
        };

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, httpRequestInitializer).setApplicationName(APPLICATION_NAME).build();

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(Instant.now().minus(1000, ChronoUnit.DAYS).getEpochSecond());
        Events events = service.events().list("primary")
                .setMaxResults(100)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }

        return items;
    }
}
