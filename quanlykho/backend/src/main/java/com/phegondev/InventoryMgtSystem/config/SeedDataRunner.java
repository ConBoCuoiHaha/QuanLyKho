package com.phegondev.InventoryMgtSystem.config;

import com.phegondev.InventoryMgtSystem.services.SeedDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDataRunner implements ApplicationRunner {

    private final SeedDataService seedDataService;

    @Value("${app.seed:false}")
    private boolean seedEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("app.seed=false -> khong tao du lieu demo khi khoi dong");
            return;
        }
        seedDataService.taoDuLieuDemo(false);
    }
}
