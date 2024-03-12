package com.notlt.Utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JavaMethodExtractor {

    public static void main(String[] args) {
        // 输入的类文件路径和方法名
//        String filePath = "/home/liyi/文档/BIT-DBench/Java/JLeaks-2023/JLeaksDataset/all_bug_files/bug-1-4c12a0694ebb63cdd72de4da2a3ac3b52a5ff47bb9b54b50030a98104f5f1af1.java";
        String filePath = "/home/liyi/文档/BIT-DBench/Java/JLeaks-2023/JLeaksDataset/all_fix_files/fix-968-9c5790a9dcea42917ce5294c012bd1d797cbadfcdaa253d030966a0f7f33c086.java";
        String methodName = "executeAndFetchFirst";

//        printAllMethods(filePath);

        Optional<String> methodText = extractMethod(filePath, methodName,412);
        methodText.ifPresent(System.out::println);



    }
    /**
     * 从Java源文件中提取指定方法的文本内容
     *
     * @param filePath   Java类文件路径
     * @param methodName 要查找的方法名
     * @return 如果找到方法，则返回其文本内容，否则返回空（Optional.empty()）
     */
    public static Optional<String> extractMethod(String filePath, String methodName) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
            List<MethodDeclaration> methodList = new ArrayList<>();
            // 遍历类中的所有方法
            for (TypeDeclaration<?> type : cu.getTypes()) {
                List<MethodDeclaration> methods = type.getMethods();
                for (MethodDeclaration method : methods) {
                    if (method.getName().asString().equals(methodName)) {
                        methodList.add(method);
                        return Optional.of(method.toString());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析文件时出错: " + filePath);
        }
        return Optional.empty();
    }

    /**
     * 从Java源文件中提取指定方法的文本内容
     *
     * @param filePath   Java类文件路径
     * @param methodName 要查找的方法名
     * @return 如果找到方法，则返回其文本内容，否则返回空（Optional.empty()）
     */
    public static Optional<String> extractMethod(String filePath, String methodName, int line_num) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
            List<MethodDeclaration> methodList = new ArrayList<>();
            // 遍历类中的所有方法
            for (TypeDeclaration<?> type : cu.getTypes()) {
                List<MethodDeclaration> methods = type.getMethods();
                for (MethodDeclaration method : methods) {
                    if (method.getName().asString().equals(methodName)) {
                        methodList.add(method);
//                        return Optional.of(method.toString());
                    }
                }
            }

            System.out.println(methodList.size());
            methodList.stream().forEach(
                    method -> {
                        System.out.println(method.getBegin().get().line);
                        System.out.println(method.getEnd().get().line);
                    }
            );


            if (methodList.isEmpty()) {
                return Optional.empty();
            } else if (methodList.size() > 1) {
                Optional<MethodDeclaration> methodDeclarationOptional = methodList.stream().filter(
                        n -> (n.getBegin().get().line - 1 <= line_num) &&
                                (n.getEnd().get().line - 1 >= line_num)
                ).findFirst();
                return methodDeclarationOptional.map(MethodDeclaration::toString);
            } else {
                return Optional.of(methodList.get(0).toString());
            }

        } catch (Exception e) {
            System.err.println("解析文件时出错: " + filePath);
        }

        return Optional.empty();
    }

    /**
     * 从指定的Java文件中提取指定方法名的所有方法声明。
     *
     * @param filePath 要解析的Java文件的路径。
     * @param methodName 要查找的方法名。
     * @return 一个包含所有匹配方法声明的列表。如果文件无法解析或方法不存在，则返回空列表。
     */
    public static List<MethodDeclaration> extractMethodList(String filePath, String methodName) {
        List<MethodDeclaration> methodList = new ArrayList<>();
        try {
            // 解析指定路径的Java文件为编译单元
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));

            // 遍历编译单元中的所有类型（类、接口等）
            for (TypeDeclaration<?> type : cu.getTypes()) {
                // 获取当前类型的全部方法声明
                List<MethodDeclaration> methods = type.getMethods();
                // 筛选出方法名匹配指定方法名的方法，并加入到方法列表中
                for (MethodDeclaration method : methods) {
                    if (method.getName().asString().equals(methodName)) {
                        methodList.add(method);
                    }
                }
            }

        } catch (Exception e) {
            // 在解析文件时遇到异常，打印错误信息
            System.err.println("解析文件时出错: " + filePath);
        }
        return methodList;
    }

    // 如果需要输出整个类的所有方法
    public static void printAllMethods(String filePath) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));

            for (TypeDeclaration<?> type : cu.getTypes()) {
                System.out.println("Methods in class " + type.getNameAsString() + ":");
                for (MethodDeclaration method : type.getMethods()) {
                    System.out.println("\t" + method.getName().toString());
                }
            }

        } catch (Exception e) {
            System.err.println("解析文件时出错: " + filePath);
        }
    }

    public static boolean isParametersEqual(MethodDeclaration methods, MethodDeclaration methodInNew) {
        if (methods.getParameters().size() != methodInNew.getParameters().size()) {
            return false;
        }
        NodeList<Parameter> parameters = methods.getParameters();
        NodeList<Parameter> parametersew = methodInNew.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).equals(parametersew.get(i))) {
                return false;
            }
        }
        return true;
    }
}
