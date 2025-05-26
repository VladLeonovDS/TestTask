package org.deathstrix.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.deathstrix.entity.TelegramUser;
import org.deathstrix.repo.TelegramUserRepository;
import org.deathstrix.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@org.springframework.stereotype.Controller
@RequestMapping("/auth")
public class Controller {

    private final TelegramUserRepository userRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    public Controller(TelegramUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/auth/telegram")
    public String loginViaTelegram(@RequestParam("initData") String initData, HttpSession session) {
        if (!AuthUtils.validateInitData(initData, botToken)) {
            return "redirect:/unauthorized";
        }

        Map<String, String> parsed = AuthUtils.parseInitData(initData);
        Map<String, Object> userData = parseUserJson(parsed.get("user"));

        Long userId = Long.parseLong(userData.get("id").toString());

        TelegramUser user = userRepository.findById(userId).orElse(new TelegramUser());

        user.setId(userId);
        user.setFirstName((String) userData.get("first_name"));
        user.setLastName((String) userData.get("last_name"));
        user.setUsername((String) userData.get("username"));
        user.setAuthDate(Long.parseLong(parsed.get("auth_date")));

        userRepository.save(user);
        session.setAttribute("user", user);

        return "redirect:/";
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        TelegramUser user = (TelegramUser) session.getAttribute("user");
        if (user == null) return "redirect:/unauthorized";

        model.addAttribute("user", user);
        return "home";
    }

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized";
    }

    private Map<String, Object> parseUserJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

}

