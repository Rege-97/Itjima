package com.itjima_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("잊지마 API 문서")
                        .description("개인 간 물건·금전 대여를 쌍방 확인하고, 반납/상환 일정을 관리하는 앱")
                        .version("1.0.0"));
    }
}
