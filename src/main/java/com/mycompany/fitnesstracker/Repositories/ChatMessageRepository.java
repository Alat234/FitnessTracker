package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.ChatMessage;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Conversation history for a canonical (low, high) participant pair, oldest first. */
    List<ChatMessage> findByUserLowAndUserHighOrderByCreatedAtAsc(User userLow, User userHigh);

    /** All messages a user takes part in (either slot) — used to discover support threads. */
    List<ChatMessage> findAllByUserLowOrUserHigh(User userLow, User userHigh);
}
