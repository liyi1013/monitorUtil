package com.notlt.Utils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GithubFileDownloader {

    public static void downloadFileFromGithub(String githubUrl, String filePath,String destinationPath) throws IOException {
        // GitHub上文件的URL格式
        String fileUrl = buildUrl(githubUrl, filePath);

        System.out.println("Downloading file from: " + fileUrl);

        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(2000);
        
        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fos = new FileOutputStream(destinationPath)) {

            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            System.out.println("File downloaded to: " + destinationPath);

        } finally {
            connection.disconnect();
        }
    }

    public static void main(String[] args) throws IOException {

        String githubUrl = "https://github.com/adempiere/adempiere/commit/e779af5d21966b08c6b7170ec501cb1ba180af97";
        String filePath =  "base/src/org/compiere/model/MAccount.java";
        String destinationPath =  "/tmp/3dddb3528a4fb671e9f53171a7d61b152da34c29b0b5e47f5de96deec9594e27";

        downloadFileFromGithub(githubUrl, filePath, destinationPath);
    }

    public static String replaceUrl(String githubUrl ){
        if (githubUrl.contains("https://github.com/")){
            String url = githubUrl.replace("https://github.com/", "https://raw.githubusercontent.com/");
            url = url.replace("commit/", "");
            return  url;
        }else if (githubUrl.contains("https://raw.githubusercontent.com/")){
            return githubUrl;
        }else {
            return null;
        }
    }

    public static String buildUrl(String githubUrl, String filePath){
        return replaceUrl(githubUrl)+"/"+filePath;
    }
}
