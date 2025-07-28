package com.openisle.service;

import com.openisle.exception.NotFoundException;
import com.openisle.model.*;
import com.openisle.repository.ActivityRepository;
import com.openisle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final LevelService levelService;
    private final NotificationService notificationService;

    public List<Activity> list() {
        return activityRepository.findAll();
    }

    public Activity getByType(ActivityType type) {
        Activity a = activityRepository.findByType(type);
        if (a == null) throw new NotFoundException("Activity not found");
        return a;
    }

    public long countLevel1Users() {
        int threshold = levelService.nextLevelExp(0);
        return userRepository.countByExperienceGreaterThanEqual(threshold);
    }

    public void end(Activity activity) {
        activity.setEnded(true);
        activityRepository.save(activity);
    }

    public void redeem(Activity activity, User user, String contact) {
        String content = user.getUsername() + " contact: " + contact;
        notificationService.createActivityRedeemNotifications(user, content);
        activity.getParticipants().add(user);
        activityRepository.save(activity);
    }
}
