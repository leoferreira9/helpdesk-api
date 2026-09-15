package com.leonardo.helpdesk.mapper;

import com.leonardo.helpdesk.dto.response.TicketHistoryResponseDto;
import com.leonardo.helpdesk.entity.TicketHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketHistoryMapper {

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    TicketHistoryResponseDto convertToResponseDto(TicketHistory ticketHistory);
}
