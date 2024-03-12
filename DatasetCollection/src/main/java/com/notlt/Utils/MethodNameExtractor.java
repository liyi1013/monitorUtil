package com.notlt.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodNameExtractor {

    // 定义一个正则表达式，匹配Java方法签名中的方法名
    private static final String METHOD_NAME_PATTERN = "(?<=\\b)[a-zA-Z_$][a-zA-Z\\d_$]*(?=\\s*\\()";

    public static String extractMethodName(String signature) {
        Pattern pattern = Pattern.compile(METHOD_NAME_PATTERN);
        Matcher matcher = pattern.matcher(signature);

        if (matcher.find()) {
            return matcher.group();
        } else {
            throw new IllegalArgumentException("Invalid method signature");
        }
    }

    public static void main(String[] args) {
        String methodSignature = "public void onSaveInstanceState(Bundle outState)";
        String methodName = extractMethodName(methodSignature);
        System.out.println("Method Name: " + methodName);

        String methodSignature2 = "no.tornado.brap.common.InvocationRequest:InvocationRequest(Method method, Object[] parameters, Serializable credentials)\n" +
                "no.tornado.brap.client:invoke(Object obj, Method method, Object[] args)";
        String methodName2 = extractMethodName(methodSignature2);
        System.out.println("Method2 Name: " + methodName2);
    }
}