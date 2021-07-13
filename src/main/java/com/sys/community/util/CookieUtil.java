package com.sys.community.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getValue(HttpServletRequest request, String keyName) {
        if (request == null || StringUtils.isBlank(keyName)) {
            throw new IllegalArgumentException("参数为空!");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(keyName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
