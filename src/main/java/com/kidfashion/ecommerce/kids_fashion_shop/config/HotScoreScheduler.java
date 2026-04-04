package com.kidfashion.ecommerce.kids_fashion_shop.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kidfashion.ecommerce.kids_fashion_shop.service.HotScoreService;

@Component
public class HotScoreScheduler {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HotScoreScheduler.class);
	private final HotScoreService hotScoreService;

	public HotScoreScheduler(HotScoreService hotScoreService) {
		this.hotScoreService = hotScoreService;
	}

	@Async
	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		try {
			log.info("[Startup] Bắt đầu tính toán điểm HOT khởi tạo trên luồng chảy ngầm...");
			this.hotScoreService.recalculateAll();
			log.info("[Startup] Tính toán điểm HOT hoàn tất.");
		} catch (Exception e) {
			log.error("[Startup] Lỗi khi tính điểm HOT khởi tạo: {}. Ứng dụng vẫn sẽ bắt đầu.", e.getMessage());
		}
	}

	@Async
	@Scheduled(fixedDelayString = "${app.hot-score.recalc-ms:300000}")
	public void recalcPeriodically() {
		try {
			this.hotScoreService.recalculateAll();
		} catch (Exception e) {
			log.warn("[Scheduler] Lỗi khi tính điểm HOT định kỳ: {}", e.getMessage());
		}
	}
}
