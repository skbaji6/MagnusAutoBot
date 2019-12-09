package org.samay.telgrambot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Data
@ConfigurationProperties(prefix="bot")
@Component
public class BotConfig {
	private String username;
	private String token;
}
