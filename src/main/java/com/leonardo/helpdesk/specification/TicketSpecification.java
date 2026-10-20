package com.leonardo.helpdesk.specification;

import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.enums.TicketPriority;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.exception.InvalidDateRangeException;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TicketSpecification {

    public static Specification<Ticket> findTicketsByStatus(TicketStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Ticket> findTicketByPriority(TicketPriority priority) {
        return (root, query, criteriaBuilder) -> {
            if (priority == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("priority"), priority);
        };
    }

    public static Specification<Ticket> findTicketByRequester(UUID requesterId) {
        return (root, query, criteriaBuilder) -> {
            if (requesterId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("requester").get("id"), requesterId);
        };
    }

    public static Specification<Ticket> findTicketByTechnician(UUID technicianId) {
        return (root, query, criteriaBuilder) -> {
            if (technicianId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("technician").get("id"), technicianId);
        };
    }

    public static Specification<Ticket> findTicketByCreatedAt(LocalDate start, LocalDate end) {
        return (root, query, criteriaBuilder) -> {
            if (start == null && end == null) {
                return criteriaBuilder.conjunction();
            }

            if (start != null && end != null && start.isAfter(end)) {
                throw new InvalidDateRangeException("Start date must not be after end date!");
            }

            if (end == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), start.atStartOfDay());
            }

            LocalDateTime endExclusive = end.plusDays(1).atStartOfDay();

            if (start == null) {
                return criteriaBuilder.lessThan(root.get("createdAt"), endExclusive);
            }

            return criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), start.atStartOfDay()),
                    criteriaBuilder.lessThan(root.get("createdAt"), endExclusive)
            );
        };
    }
}
