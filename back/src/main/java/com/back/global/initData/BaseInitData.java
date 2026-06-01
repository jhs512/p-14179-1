package com.back.global.initData;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class BaseInitData {

    private final InitDataService initDataService;

    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> initDataService.seedSampleData();
    }
}
