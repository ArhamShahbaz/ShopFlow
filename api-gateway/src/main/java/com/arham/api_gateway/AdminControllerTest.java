package com.arham.api_gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminControllerTest {


    @GetMapping()
    public  String hello() {
        return "Hello, Admin ";
    }
}
