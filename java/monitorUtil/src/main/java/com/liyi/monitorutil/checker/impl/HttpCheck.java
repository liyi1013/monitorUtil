package com.liyi.monitorutil.checker.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.liyi.monitorutil.checker.Checker;
import org.springframework.stereotype.Service;

@Service
public class HttpCheck implements Checker {
    public boolean check(String url){
        try(HttpResponse response = HttpRequest.get(url).timeout(10*1000).execute()) {
            if (response.getStatus()>= HttpStatus.HTTP_BAD_REQUEST){
                return false;
            }else {
                return true;
            }
        } catch (Exception e){
            return false;
        }
    }
}
