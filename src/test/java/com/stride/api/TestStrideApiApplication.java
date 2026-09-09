package com.stride.api;

import org.springframework.boot.SpringApplication;

public class TestStrideApiApplication {

  public static void main(String[] args) {
    SpringApplication.from(StrideApiApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
