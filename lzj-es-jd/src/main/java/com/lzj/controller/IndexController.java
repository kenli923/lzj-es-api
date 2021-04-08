package com.lzj.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class IndexController {

    @GetMapping({"/", "/index"})
    public String Index() {
        return "index";
    }
}
