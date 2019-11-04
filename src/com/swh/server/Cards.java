package com.swh.server;

import com.swh.client.MathExpException;
import com.swh.client.MyCalculator;

import java.util.*;

public class Cards {
    private Iterator cardIterator;

    /**
     * 生成一副打乱的卡牌对象
     * */
    Cards() {
        List cards = Arrays.asList("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K",
                "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K",
                "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K",
                "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K");
        Collections.shuffle(cards);
        cardIterator = cards.iterator();
    }

    /**
     * 获取下一组随机4张卡牌，迭代完毕就返回null
     * */
    String[] next() {
        if (!cardIterator.hasNext())
            return null;
        String[] group = new String[4];
        for(int i = 0; i < 4; i++)
            group[i] = (String) cardIterator.next();
        return group;
    }

    /**
     * 对答案进行验证
     * @param answer : 玩家输入的答案
     * @param group : 所需要使用的4张卡牌
     * @return : 符合规则，且答案正确，返回true; 否则返回false
     * */
    public static boolean checkAnswer(String answer, String[] group) {
        /* 将字母转换为数字，方便后面处理 */
        for (int i = 0; i < 4; i++) {
            switch (group[i]) {
                case "A":
                    group[i] = "1";
                    answer = answer.replace("A", "1");
                    answer = answer.replace("a", "1");
                    break;
                case "J":
                    group[i] = "11";
                    answer = answer.replace("J", "11");
                    answer = answer.replace("j", "11");
                    break;
                case "Q":
                    group[i] = "12";
                    answer = answer.replace("Q", "12");
                    answer = answer.replace("q", "12");
                    break;
                case "K":
                    group[i] = "13";
                    answer = answer.replace("K", "13");
                    answer = answer.replace("k", "13");
                    break;
            }
        }
        //将中文括号转换为英文括号，增大容错率
        answer = answer.replace("（","(");
        answer = answer.replace("）",")");
        if (!isConformToRules(answer, group)) return false;    //检查是否符合规则
        try {
            int result = (int) MyCalculator.convert(answer);    //计算答案
            if (result == 24) return true;
        } catch (MathExpException ignored) {}
        return false;
    }

    /**
     * 检查答案是否符合规则 ：
     *     卡牌是否使用完；是否使用了其他的卡牌
     *     括号是否对其
     *     运算符号是否使用正确
     * @param answer : 转换为数字后的算数表达式
     * @param group : 转换为数字后的卡牌
     * */
    private static boolean isConformToRules(String answer, String[] group) {
        /* 将“1”全部放到后面去，以免在后面删除“1”时将“1x”的1删掉 */
        Arrays.sort(group, (o1, o2) -> {
            if (!o1.equals("1") && o2.equals("1")) return -1;
            return 0;
        });
        /* 依次将字符中应该使用的数字删掉，方便后面检查 */
        String temp;
        for (int i = 0; i < 4; i++) {
            temp = answer.replaceFirst(group[i], "");
            if (temp.equals(answer)) return false;  //替换失败，说明卡牌没有使用完
            answer = temp;
        }
        int r = 0;  //匹对括号，最后应该为0
        int ops = 0;    //运算符号个数，最后应该为3
        for(int i=0; i<answer.length(); i++) {
            switch (answer.charAt(i)) {
                case '(': r++; break;
                case ')': r--; break;
                case '+':
                case '-':
                case '/':
                case '*':
                    ops++; break;
                default:
                    return false;
            }
        }
        return r == 0 && ops == 3;
    }
}
