package com.sugarcane.erp.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MarathiTransliterator {

    public static void attach(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.endsWith(" ")) {
                // If user typed a space, we process the last word
                int lastSpace = newValue.lastIndexOf(' ', newValue.length() - 2);
                String lastWord = (lastSpace == -1) ? newValue.substring(0, newValue.length() - 1) : newValue.substring(lastSpace + 1, newValue.length() - 1);
                
                // Only transliterate if it contains english letters
                if (lastWord.matches(".*[a-zA-Z]+.*")) {
                    new Thread(() -> {
                        String transliterated = fetchMarathi(lastWord);
                        if (transliterated != null && !transliterated.isEmpty()) {
                            Platform.runLater(() -> {
                                String newText;
                                if (lastSpace == -1) {
                                    newText = transliterated + " ";
                                } else {
                                    newText = newValue.substring(0, lastSpace + 1) + transliterated + " ";
                                }
                                // Update text only if it has not changed while waiting
                                if (textField.getText().equals(newValue)) {
                                    textField.setText(newText);
                                    textField.positionCaret(newText.length());
                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }

    private static String fetchMarathi(String englishWord) {
        try {
            String encodedWord = URLEncoder.encode(englishWord, StandardCharsets.UTF_8.toString());
            String urlStr = "https://inputtools.google.com/request?text=" + encodedWord + "&itc=mr-t-i0-und&num=1&cp=0&cs=1&ie=utf-8&oe=utf-8&app=test";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            
            String json = content.toString();
            // Google Input Tools JSON response format:
            // ["SUCCESS",[["shahaji",["शहाजी"],[],{"annotation":[""],"candidate_type":[0],"transliteration":[{"cache_dict_std_deriv":true,"transform_dict_std_deriv":true}]}],...]]
            
            int bracketIndex = json.indexOf(",[\"");
            if (bracketIndex != -1) {
                int endQuote = json.indexOf("\"]", bracketIndex);
                if (endQuote != -1) {
                    return json.substring(bracketIndex + 3, endQuote);
                }
            }
            
        } catch (Exception e) {
            // Silently ignore network errors to not interrupt typing
        }
        return null;
    }
}
