package com.notlt.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FileUtil {

    public static void writeStringToFile(String content, String filePath) {
        // 参数检验
        if (content == null || filePath == null) {
            throw new IllegalArgumentException("内容和文件路径不能为空");
        }

        File file = new File(filePath);
        if (!Objects.isNull(file.getParentFile())){
            System.out.println(file.getParentFile().toString());
            System.out.println(file.getParentFile().exists());
            // 检查文件路径是否存在，若不存在则尝试创建路径
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                System.err.println("无法创建文件路径: " + file.getParent());
                return;
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 写入字符串内容到文件
            writer.write(content);
        } catch (IOException e) {
            // 错误处理，考虑使用日志框架记录错误
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 示例：提供一个检查文件是否存在的辅助方法
    public static boolean fileExists(String filePath) {
        // 参数校验
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("文件路径为空或无效");
            return false;
        }

        // 防止路径遍历攻击，剥离相对路径符号
        // 注意：这里只是一个简单的示例，可能需要根据实际情况调整规则
        if (filePath.contains("..")) {
            System.err.println("文件路径包含非法字符");
            return false;
        }

        Path path = Paths.get(filePath);
        try {
            // 检查文件是否存在
            return Files.exists(path);
        } catch (Exception e) {
            // 异常处理，例如记录日志
            e.printStackTrace();
            // 根据实际情况，可以选择返回false或抛出自定义异常
            return false;
        }
    }

    public static void main(String[] args) {
        String testFilePath = "test.txt";
        System.out.println("文件存在性检查结果: " + fileExists(testFilePath));

        FileUtil.writeStringToFile("test", testFilePath);
    }
}
