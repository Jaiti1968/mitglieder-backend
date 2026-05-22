package de.emc.mitglieder.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemInfoController {

    private final BuildProperties buildProperties;

    public SystemInfoController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/api/system/info")
    public SystemInfoDto getSystemInfo() {
        return new SystemInfoDto(buildProperties.getVersion());
    }

    public record SystemInfoDto(String backendVersion) {
    }
}