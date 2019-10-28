package com.swh;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Cards {
    private Iterator cardIterator;
    public Cards() {
        List cards = Arrays.asList("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K",
                "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K",
                "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K",
                "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K");
        Collections.shuffle(cards);
        cardIterator = cards.iterator();
    }

    public String[] next() {
        if (!cardIterator.hasNext())
            return null;
        String[] group = new String[4];
        for(int i = 0; i < 4; i++)
            group[i] = (String) cardIterator.next();
        return group;
    }

    public static String getDigital(String card) {
        switch (card) {
            case "A":
            case "a": return "1";
            case "J":
            case "j": return "11";
            case "Q":
            case "q": return "12";
            case "K":
            case "k": return "13";
            //case "1": return "10";
            default: return card;
        }
    }

    public static boolean checkAnswer(String answer, String[] group) {
        for (String card : group) {
            switch (card) {
                case "A":
                    answer = answer.replace("A", "1");
                    answer = answer.replace("a", "1");
                    break;
                case "J":
                    answer = answer.replace("J", "11");
                    answer = answer.replace("j", "11");
                    break;
                case "Q":
                    answer = answer.replace("Q", "12");
                    answer = answer.replace("q", "12");
                    break;
                case "K":
                    answer = answer.replace("K", "13");
                    answer = answer.replace("k", "13");
                    break;
            }
        }
        System.out.println(answer);
        try {
            int result = (int) MyCalculator.convert(answer);
            if (result == 24) return true;
        } catch (MathExpException e) {
            e.printStackTrace();
        }
        return false;
    }

}
