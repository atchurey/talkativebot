package com.atchurey.talkativebot.utils;

import java.util.UUID;

public class Utils {

    public static String generateUniqueID() {
        return UUID.randomUUID().toString();
    }

    public static boolean isEmptyOrNull(String input) {
        return input == null || input.length() == 0;
    }

}

