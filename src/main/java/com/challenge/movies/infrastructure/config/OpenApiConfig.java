package com.challenge.movies.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Movies API")
                .version("1.0.0")
                .description(
                    "REST API to retrieve directors with movie count above a given threshold. "
                        + "This API fetches movies from an external source and filters directors based on the number of movies they have directed.")
                .contact(new Contact().name("Movies API Support").email("support@moviesapi.com")))
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("Local development server")));
  }
}
