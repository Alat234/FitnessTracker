package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.Notification;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** All of a user's notifications, newest first. */
    List<Notification> findAllByRecipientOrderByCreatedAtDesc(User recipient);

    /** A user's unread notifications, newest first. */
    List<Notification> findAllByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);

    /** Unread count for the bell badge. */
    long countByRecipientAndReadFalse(User recipient);
}
