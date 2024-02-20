package com.liyi.monitorutil.checker.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.liyi.monitorutil.checker.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * HttpCheck类实现了Checker接口，用于检查HTTP请求是否成功。
 */
@Service
public class HttpCheck implements Checker {

    /**
     * The logger used to log information about the HttpCheck class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCheck.class);


    /**
     * Check if a given URL is accessible.
     *
     * @param url the URL to check
     * @return true if the URL is accessible, false otherwise
     */
    public boolean check(String url) {
        try (HttpResponse response = HttpRequest.get(url).timeout(10 * 1000).execute()) {
            if (response.getStatus() >= HttpStatus.HTTP_BAD_REQUEST) {
                return false;
            }
            LOGGER.info("HTTP request successful for URL: {}", url);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error checking URL: {}, error: {}", url, e.getMessage(), e);
            return false;
        }
    }
}

