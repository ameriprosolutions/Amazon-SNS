package com.JFFleetOperate.server.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.JFFleetOperate"})
public class FleetOperateApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FleetOperateApiApplication.class, args);
	}
}
