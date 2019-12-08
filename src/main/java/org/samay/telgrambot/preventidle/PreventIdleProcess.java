package org.samay.telgrambot.preventidle;

import java.util.Calendar;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PreventIdleProcess {

	@Scheduled(cron = "*/30 * * * * ?")
	public void run() {
		System.out.println("Current time is :: " + Calendar.getInstance().getTime());
	}

}
