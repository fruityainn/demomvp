package com.hide.videophoto.ui.security.password;

import java.util.Stack;

import androidx.annotation.NonNull;

import static java.lang.Math.pow;

class Eval {
    @NonNull
    private static String refine(@NonNull String infix) {
        final StringBuilder infixBuilder = new StringBuilder(infix);
        final int parenthesesDifferent = countChar(infixBuilder, '(')
                - countChar(infixBuilder, ')');
        if (parenthesesDifferent > 0) {
            infixBuilder.append(
                    String.valueOf(new char[parenthesesDifferent])
                            .replace('\0', ')')
            );
        } else if (parenthesesDifferent < 0 || !balancedParentheses(infix)) {
            return "";
        }

        return infixBuilder.toString()
                .replaceAll("\\s+", "")
                .replaceAll("([(*/%^])-(\\d+(\\.(\\d+)?)?)", "$1(0-$2)")
                .replaceAll("([(*/%^])-\\(", "$1(-1)*(")
                .replaceAll("\\)\\(", ")*(")
                .replaceAll("(\\d)\\(", "$1*(")
                .replaceAll("[+\\-*/%^()]", "$0 ")
                .replaceAll("\\d+(\\.(\\d+)?)?", "$0 ")
                .trim();
    }

    private static boolean balancedParentheses(@NonNull String s) {
        final Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(':
                    stack.push(c);
                    break;
                case ')':
                    if (stack.isEmpty()) {
                        return false;
                    }
                    stack.pop();
                    break;
            }
        }
        return stack.isEmpty();
    }

    private static int countChar(@NonNull CharSequence s, char ch) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (ch == s.charAt(i)) ++count;
        }
        return count;
    }

    static double execute(@NonNull String infix) {
        final String postfix = infixToPostfix(infix);
        return evaluation(postfix);
    }

    private static double evaluation(@NonNull String postfix) {
        final Stack<Double> stack = new Stack<>();
        for (String s : postfix.trim().split("\\s+")) {
            if ("+-*/%^".contains(s)) {
                final double y = stack.pop();
                final double x = stack.pop();

                double res;
                switch (s.charAt(0)) {
                    case '+':
                        res = x + y;
                        break;
                    case '-':
                        res = x - y;
                        break;
                    case '*':
                        res = x * y;
                        break;
                    case '/':
                        res = x / y;
                        break;
                    case '%':
                        res = x % y;
                        break;
                    case '^':
                        res = pow(x, y);
                        break;
                    default:
                        throw new IllegalStateException("Unknown operator '" + s.charAt(0) + "'");
                }

                stack.push(res);
            } else {
                stack.push(Double.parseDouble(s));
            }
        }
        return stack.peek();
    }

    @NonNull
    private static String infixToPostfix(@NonNull String infix) {
        final StringBuilder postfix = new StringBuilder();
        final Stack<String> stack = new Stack<>();

        for (String elem : refine(infix).split("\\s+")) {
            if ("+-*/^".contains(elem)) {
                while (!stack.isEmpty()
                        && priorityOf(elem) <= priorityOf(stack.peek())) {
                    postfix.append(stack.pop())
                            .append(' ');
                }
                stack.push(elem);
            } else if ("(".equals(elem)) {
                stack.push(elem);
            } else if (")".equals(elem)) {
                while (!"(".equals(stack.peek())) {
                    postfix.append(stack.pop())
                            .append(' ');
                }
                stack.pop();
            } else {
                postfix.append(elem).append(' ');
            }
        }

        while (!stack.isEmpty()) {
            postfix.append(stack.pop()).append(' ');
        }

        return postfix.toString();
    }

    private static int priorityOf(@NonNull String operator) {
        if ("^".equals(operator)) return 3;
        if ("*/%".contains(operator)) return 2;
        if ("+-".contains(operator)) return 1;
        if ("()".contains(operator)) return 0;
        throw new IllegalStateException("Operator '" + operator + "' not implement");
    }
}
