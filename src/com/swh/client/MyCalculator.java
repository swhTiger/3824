package com.swh.client;

import java.util.Stack;

/**
 * 取自于 Java实验二
 * 计算字符串表达式
 * */
public class MyCalculator {
    private static int check_char(char ch) throws MathExpException {
        if(('0'<=ch && ch<='9') || ch=='.')
            return 1;
        else if(ch=='+' || ch=='-' || ch=='*' || ch=='/')
            return 2;
        else if(ch=='(')
            return 3;
        else if(ch==')')
            return 4;
        else
            throw new MathExpException();
    }

    private static double math_exp(String s) throws MathExpException {
        Stack<Double> num = new Stack<>();
        double n;
        for(int i=0; i<s.length(); i++)
        {
            char a = s.charAt(i);
            if(a == '(')
            {
                n=math_exp(s.substring(i+1));
                num.push(n);
                int r=0;
                do{
                    if(s.charAt(i) == '(')
                        r++;
                    else if(s.charAt(i) == ')')
                        r--;
                    i++;
                }while(r!=0);
            }
            else if(a == ')')
            {
                n=0;
                while(!num.isEmpty())
                    n+=num.pop();
                return n;
            }
            else if(a=='*' || a=='/')
            {
                double tem = num.pop();
                if(s.charAt(i+1) == '(')
                {
                    n = math_exp(s.substring(i+2));
                    int r=0;
                    i++;
                    do{
                        if(s.charAt(i) == '(')
                            r++;
                        else if(s.charAt(i) == ')')
                            r--;
                        i++;
                    }while(r!=0);
                }
                else
                {
                    int start = i++;
                    for(; i<s.length(); i++)
                        if(check_char(s.charAt(i)) != 1)
                            break;
                    try {
                        n = Double.parseDouble(s.substring(start+1, i));
                    }catch(NumberFormatException e){
                        throw new MathExpException("���ʽ����");
                    }
                }
                if(a=='*') num.push(tem*n);
                else num.push(tem/n);
            }
            else if(a=='+' || a=='-')
            {
                if(s.charAt(i+1) == '(')
                {
                    n = math_exp(s.substring(i+2));
                    int r=0;
                    i++;
                    do{
                        if(s.charAt(i) == '(')
                            r++;
                        else if(s.charAt(i) == ')')
                            r--;
                        i++;
                    }while(r!=0);
                }
                else
                {
                    int start = i++;
                    for(; i<s.length(); i++)
                        if(check_char(s.charAt(i)) != 1)
                            break;
                    try {
                        n = Double.parseDouble(s.substring(start+1, i));
                    }catch(NumberFormatException e){
                        throw new MathExpException();
                    }
                }
                if(a=='-') n=-n;
                num.push(n);
            }
            else if(i == 0)
            {
                int start = i++;
                for(; i<s.length(); i++)
                    if(check_char(s.charAt(i)) != 1)
                        break;
                try {
                    n = Double.parseDouble(s.substring(start, i));
                }catch(NumberFormatException e){
                    throw new MathExpException();
                }
                num.push(n);
            }
            i--;
        }
        n=0;
        while(!num.isEmpty())
            n+=num.pop();
        return n;
    }

    public static double convert(String s) throws MathExpException {
        return math_exp(s);
    }
}
