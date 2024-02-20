package com.liyi.monitorutil.checker.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.liyi.monitorutil.checker.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HttpCheck implements Checker {

    private static final Logger LOGGER  = LoggerFactory.getLogger(HttpCheck.class);
    public boolean check(String url){
        LOGGER.info("url:{}",url);
        try(HttpResponse response = HttpRequest.get(url).timeout(10*1000).execute()) {
            if (response.getStatus()>= HttpStatus.HTTP_BAD_REQUEST){
                return false;
            }else {
                return true;
            }
        } catch (Exception e){
            LOGGER.error("httpCheck error.",e);
            return false;
        }
    }
}
