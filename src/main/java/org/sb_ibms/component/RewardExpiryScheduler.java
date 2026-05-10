package org.sb_ibms.component;

import org.sb_ibms.services.RewardService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@EnableScheduling
public class RewardExpiryScheduler {

    private final RewardService rewardService;

    public RewardExpiryScheduler(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Scheduled(cron = "0 0 2 * * *")   // Every day at 2 AM
    public void expirePoints() {
        System.out.println("🔄 Running daily points expiry job...");
        rewardService.expireOldPoints();
    }
}