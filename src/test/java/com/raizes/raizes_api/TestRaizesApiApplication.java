package com.raizes.raizes_api;

import org.springframework.boot.SpringApplication;

public class TestRaizesApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(RaizesApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
