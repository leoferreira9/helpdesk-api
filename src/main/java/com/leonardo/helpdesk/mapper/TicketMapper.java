package com.leonardo.helpdesk.mapper;

import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "requesterName", source = "requester.name")
    @Mapping(target = "requesterEmail", source = "requester.email")
    @Mapping(target = "requesterRole", source = "requester.role")
    @Mapping(target = "technicianName", source = "technician.name")
    @Mapping(target = "technicianEmail", source = "technician.email")
    TicketResponseDto convertToResponseDto(Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "technician", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    Ticket convertToEntity(TicketRequestDto requestDto);
}
