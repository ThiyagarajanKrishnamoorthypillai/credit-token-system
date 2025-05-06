package com.example.token.service;

import java.util.HashSet;
import java.util.Set;

public class EmailUtils {
    private static final Set<String> disposableDomains = new HashSet<>();

    static {
        // Add more domains as needed
        disposableDomains.add("mailinator.com");
        disposableDomains.add("tempmail.com");
        disposableDomains.add("10minutemail.com");
        disposableDomains.add("yopmail.com");
        disposableDomains.add("dispostable.com");
    }

    public static boolean isDisposable(String email) {
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        return disposableDomains.contains(domain);
    }
}
