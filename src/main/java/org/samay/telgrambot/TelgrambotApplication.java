package org.samay.telgrambot;

import org.samay.telgrambot.bot.MagnusAutoBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
@EnableScheduling
public class TelgrambotApplication implements CommandLineRunner {
	@Autowired
	private MagnusAutoBot magnusAutoBot;

	static {
		ApiContextInitializer.init();
	}

	public static void main(String[] args) {
		SpringApplication.run(TelgrambotApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		TelegramBotsApi botsApi = new TelegramBotsApi();
		botsApi.registerBot(magnusAutoBot);
	}

}
