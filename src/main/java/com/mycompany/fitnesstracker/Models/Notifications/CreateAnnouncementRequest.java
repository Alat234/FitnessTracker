package com.mycompany.fitnesstracker.Models.Notifications;

import com.mycompany.fitnesstracker.Models.Enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin request to broadcast a system announcement. {@code targetRole} null →
 * every user; otherwise only users with that role. Fanned out to per-recipient rows.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateAnnouncementRequest {
    private String title;
    private String message;
    private Role targetRole;
}
