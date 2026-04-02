package com.kidfashion.ecommerce.kids_fashion_shop.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kidfashion.ecommerce.kids_fashion_shop.service.HotScoreService;

@Component
public class HotScoreScheduler {

	private final HotScoreService hotScoreService;

	public HotScoreScheduler(HotScoreService hotScoreService) {
		this.hotScoreService = hotScoreService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		this.hotScoreService.recalculateAll();
	}

	@Scheduled(fixedDelayString = "${app.hot-score.recalc-ms:300000}")
	public void recalcPeriodically() {
		this.hotScoreService.recalculateAll();
	}
}
