package com.github.gquintana.kafka.brod.util;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class Strings {
    public static String toSnakeCase(String s) {
        List<String> words = new ArrayList<>();
        String word;
        StringBuilder wordBuilder = new StringBuilder();
        boolean lastWasLower = false;
        for(char c: s.toCharArray()) {
            if (Character.isUpperCase(c) || !Character.isLetterOrDigit(c)) {
                if (lastWasLower) {
                    word = wordBuilder.toString();
                    words.add(word);
                    wordBuilder.delete(0, word.length());
                }
                lastWasLower = false;
            } else {
                lastWasLower = true;
            }
            if (Character.isLetterOrDigit(c)) {
                wordBuilder.append(c);
            }
        }
        word = wordBuilder.toString();
        if (!word.isEmpty()) {
            words.add(word);
        }
        return words.stream().map(String::toLowerCase).collect(joining("_"));
    }
}
