package org.samay.telgrambot.config.beans;

import org.samay.telgrambot.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.facilities.filedownloader.TelegramFileDownloader;

@Configuration
public class BeanConfiguration {
	@Autowired
	private BotConfig botConfig;
	
	@Bean
	public TelegramFileDownloader telegramFileDownloader() {
		TelegramFileDownloader downloader=new TelegramFileDownloader((() -> botConfig.getToken()));
		return downloader;
	}

}
