package com.liyi.monitorutil.controller;

import com.liyi.monitorutil.checker.impl.HttpCheck;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/check")
public class CheckController {
    @Resource
    HttpCheck httpCheck;

    @PostMapping(value = "/httpCheck")
    public String httpCheck(@RequestBody String url){
        return String.valueOf(httpCheck.check(url));
    }

    @PostMapping(value = "/multiCheck")
    public void multiCheck(){
        // TODO
        return;
    }
}
