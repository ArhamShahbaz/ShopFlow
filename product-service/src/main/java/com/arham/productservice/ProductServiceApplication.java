package com.arham.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

//	@Bean
//	public RouteLocator routerBuilder(RouteLocatorBuilder routeLocatorBuilder){
//		return routeLocatorBuilder.routes()
//				.route("Microservice1",r->r.path("/serviceA/**")
//						.uri("http://localhost:8081/"))
//				.route("Microservice2",r->r.path("/serviceB/**")
//						.uri("http://localhost:8082/")).build();
//	}

}
