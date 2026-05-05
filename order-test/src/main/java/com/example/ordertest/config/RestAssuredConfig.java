package com.example.ordertest.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Configuration
public class RestAssuredConfig {

    @Value("${rest-assured.base-uri:http://localhost}")
    private String baseUri;

    @Value("${rest-assured.base-path:/api}")
    private String basePath;

    @Value("${rest-assured.port:8080}")
    private int port;

    @PostConstruct
    public void setup() {
        RestAssured.baseURI = baseUri;
        RestAssured.basePath = basePath;
        RestAssured.port = port;
        RestAssured.filters(Arrays.asList(
                new AllureRestAssured(),
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
        ));
    }
}
