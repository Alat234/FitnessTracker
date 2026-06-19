package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Chat.ChatMessageDTO;
import com.mycompany.fitnesstracker.Models.Chat.ChatPartnerDTO;
import com.mycompany.fitnesstracker.Models.Chat.SendMessageRequest;
import com.mycompany.fitnesstracker.Services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Trainer-client chat. Access (accepted TRAINER pair, supported roles) is
 * enforced in ChatService.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/partners")
    public ResponseEntity<List<ChatPartnerDTO>> partners() {
        return ResponseEntity.ok(chatService.getPartners());
    }

    @GetMapping("/{partnerId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> messages(@PathVariable Long partnerId) {
        return ResponseEntity.ok(chatService.getMessages(partnerId));
    }

    @PostMapping("/{partnerId}/messages")
    public ResponseEntity<ChatMessageDTO> send(@PathVariable Long partnerId,
                                               @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.send(partnerId, request.getText()));
    }
}
