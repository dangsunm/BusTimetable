package com.bustime.module;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class MessageHandler {
    public String showMessageAndRedirect(final MessageDto params, Model model) {
        model.addAttribute("params", params);
        return "messageRedirect";
    }
}
