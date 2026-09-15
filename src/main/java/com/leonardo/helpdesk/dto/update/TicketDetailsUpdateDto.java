package com.leonardo.helpdesk.dto.update;

import com.leonardo.helpdesk.enums.TicketPriority;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TicketDetailsUpdateDto(
        @Size(max = 150) @Pattern(regexp = ".*\\S.*") String title,
        @Size(max = 10000) @Pattern(regexp = ".*\\S.*") String description,
        TicketPriority priority
) {
    @AssertTrue(message = "Please provide at least one field to update")
    public boolean isAtLeastOneFieldProvided() {
        return title != null || description != null || priority != null;
    }
}
