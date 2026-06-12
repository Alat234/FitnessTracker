package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Connection.ConnectionDTO;
import com.mycompany.fitnesstracker.Models.Connection.SendInviteRequest;
import com.mycompany.fitnesstracker.Services.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    /* ── Invites ──────────────────────────────────────────── */

    /** Надіслати запрошення користувачу за email (тип: TRAINER або FRIEND). */
    @PostMapping("/invites")
    public ResponseEntity<ConnectionDTO> sendInvite(@RequestBody SendInviteRequest request) {
        String email = currentUserEmail();
        ConnectionDTO created = connectionService.sendInvite(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Мої вихідні запрошення (я — власник даних). */
    @GetMapping("/outgoing")
    public ResponseEntity<List<ConnectionDTO>> getOutgoing() {
        String email = currentUserEmail();
        return ResponseEntity.ok(connectionService.getOutgoing(email));
    }

    /** Запрошення мені (я — viewer): PENDING + ACCEPTED. */
    @GetMapping("/incoming")
    public ResponseEntity<List<ConnectionDTO>> getIncoming() {
        String email = currentUserEmail();
        return ResponseEntity.ok(connectionService.getIncoming(email));
    }

    /* ── Responses ────────────────────────────────────────── */

    /** Прийняти запрошення (лише запрошений користувач). */
    @PostMapping("/{id}/accept")
    public ResponseEntity<ConnectionDTO> accept(@PathVariable Long id) {
        String email = currentUserEmail();
        return ResponseEntity.ok(connectionService.accept(id, email));
    }

    /** Відхилити запрошення (лише запрошений користувач). */
    @PostMapping("/{id}/decline")
    public ResponseEntity<ConnectionDTO> decline(@PathVariable Long id) {
        String email = currentUserEmail();
        return ResponseEntity.ok(connectionService.decline(id, email));
    }

    /** Відкликати доступ (лише власник даних). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        String email = currentUserEmail();
        connectionService.revoke(id, email);
        return ResponseEntity.noContent().build();
    }

    /* ── helpers ──────────────────────────────────────────── */
    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
