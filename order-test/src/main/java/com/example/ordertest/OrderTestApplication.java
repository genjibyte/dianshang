package com.example.ordertest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.ordertest.data.mapper")
public class OrderTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderTestApplication.class, args);
    }
}
