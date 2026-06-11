package de.emc.mitglieder.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;

@RestController
public class SystemInfoController {

    private final BuildProperties buildProperties;
    private final Environment environment;

    public SystemInfoController(
            BuildProperties buildProperties,
            Environment environment
    ) {
        this.buildProperties = buildProperties;
        this.environment = environment;
    }

    @GetMapping("/api/system/info")
    public SystemInfoDto getSystemInfo() {
        return new SystemInfoDto(
                buildProperties.getVersion(),
                determineEnvironment(),
                environment.getActiveProfiles(),
                buildProperties.getTime()
        );
    }

    private String determineEnvironment() {
        String[] profiles = environment.getActiveProfiles();

        if (profiles.length == 0) {
            return "LOCAL";
        }

        if (Arrays.asList(profiles).contains("prod")) {
            return "PROD";
        }

        if (Arrays.asList(profiles).contains("dev")) {
            return "DEV";
        }

        return "LOCAL";
    }

    public record SystemInfoDto(
            String backendVersion,
            String environment,
            String[] activeProfiles,
            Instant buildTime
    ) {
    }
}