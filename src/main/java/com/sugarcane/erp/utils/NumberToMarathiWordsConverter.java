package com.sugarcane.erp.utils;

public class NumberToMarathiWordsConverter {

    private static final String[] units = {
            "", "एक", "दोन", "तीन", "चार", "पाच", "सहा", "सात", "आठ", "नऊ", "दहा",
            "अकरा", "बारा", "तेरा", "चौदा", "पंधरा", "सोळा", "सतरा", "अठरा", "एकोणीस"
    };

    private static final String[] tens = {
            "", "", "वीस", "तीस", "चाळीस", "पन्नास", "साठ", "सत्तर", "ऐंशी", "नव्वद"
    };

    public static String convert(long n) {
        if (n == 0) {
            return "शून्य";
        }
        if (n < 0) {
            return "उणे " + convert(Math.abs(n));
        }

        String words = "";

        if ((n / 10000000) > 0) {
            words += convert(n / 10000000) + " कोटी ";
            n %= 10000000;
        }

        if ((n / 100000) > 0) {
            words += convert(n / 100000) + " लाख ";
            n %= 100000;
        }

        if ((n / 1000) > 0) {
            words += convert(n / 1000) + " हजार ";
            n %= 1000;
        }

        if ((n / 100) > 0) {
            words += convert(n / 100) + "शे ";
            n %= 100;
        }

        if (n > 0) {
            if (n < 20) {
                words += units[(int) n];
            } else {
                words += tens[(int) (n / 10)];
                if ((n % 10) > 0) {
                    words += " " + units[(int) (n % 10)];
                }
            }
        }

        return words.trim();
    }
}
