package com.upb.zadanie3.security;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;


@Component
public class HtmlInputFieldsCheck {

    public boolean isHtml(String input) {
        boolean isHtml = false;
        if (input != null) {
            if (!input.equals(HtmlUtils.htmlEscape(input))) {
                isHtml = true;
            }
        }
        return isHtml;
    }

}
