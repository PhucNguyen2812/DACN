package com.DACN.quanlikhoa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuanlikhoaApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuanlikhoaApplication.class, args);
		System.out.println("========================================");
        System.out.println("🚀 Hệ thống Quản lý Khoa CNTT đã khởi động!");
        System.out.println("📝 API Base URL: http://localhost:8080/api");
        System.out.println("🔐 Login endpoint: POST /api/auth/login");
        System.out.println("========================================");
	}

}
