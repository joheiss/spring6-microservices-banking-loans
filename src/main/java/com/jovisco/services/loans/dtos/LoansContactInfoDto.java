package com.jovisco.services.loans.dtos;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loans")
public record LoansContactInfoDto(
                String message,
                Map<String, String> contact,
                List<String> support) {
}
