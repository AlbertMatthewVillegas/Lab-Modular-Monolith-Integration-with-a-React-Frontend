package educ.cit.villegas.notification.service;

import educ.cit.villegas.notification.entity.Notification;
import educ.cit.villegas.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotifications() {
        return notificationRepository.findAll();
    }
}