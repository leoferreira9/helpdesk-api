package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.dto.update.TicketDetailsUpdateDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.*;
import com.leonardo.helpdesk.mapper.TicketMapper;
import com.leonardo.helpdesk.repository.TicketRepository;
import com.leonardo.helpdesk.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TicketService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository, TicketMapper ticketMapper, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketMapper = ticketMapper;
    }

    private Ticket findTicketOrThrow(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));
    }

    public User findUserOrThrow(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Transactional
    public TicketResponseDto create(TicketRequestDto requestDto) {
        User requesterExists = userRepository.findById(requestDto.requesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found with ID: " + requestDto.requesterId()));

        if(!requesterExists.isActive()){
            throw new UserNotActiveException("Requester is not active");
        }

        Ticket ticket = ticketMapper.convertToEntity(requestDto);
        ticket.setRequester(requesterExists);

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.convertToResponseDto(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketResponseDto findById(UUID ticketId){
        Ticket ticketExists = findTicketOrThrow(ticketId);
        return ticketMapper.convertToResponseDto(ticketExists);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponseDto> findAll(Pageable pageable) {
        Page<Ticket> page = ticketRepository.findAll(pageable);
        return page.map(ticketMapper::convertToResponseDto);
    }

    @Transactional
    public TicketResponseDto updateDetails(UUID ticketId, TicketDetailsUpdateDto detailsUpdateDto) {
        Ticket ticketExists = findTicketOrThrow(ticketId);

        if(ticketExists.getStatus().equals(TicketStatus.CLOSED)) {
            throw new InvalidTicketStatusException("Ticket can't be updated because it's Closed");
        }

        if(detailsUpdateDto != null){
            if(detailsUpdateDto.title() != null && !detailsUpdateDto.title().isBlank()) {
                ticketExists.setTitle(detailsUpdateDto.title());
            }

            if(detailsUpdateDto.description() != null && !detailsUpdateDto.description().isBlank()){
                ticketExists.setDescription(detailsUpdateDto.description());
            }

            if(detailsUpdateDto.priority() != null){
                ticketExists.setPriority(detailsUpdateDto.priority());
            }
        }

        Ticket savedTicket = ticketRepository.save(ticketExists);
        return ticketMapper.convertToResponseDto(savedTicket);
    }

    @Transactional
    public TicketResponseDto assignTechnician(UUID ticketId, UUID technicianId) {
        Ticket ticketExists = findTicketOrThrow(ticketId);
        User technicianExists = findUserOrThrow(technicianId);

        if(!ticketExists.getStatus().equals(TicketStatus.OPEN)) {
            throw new InvalidTicketStatusException("Ticket must be Open to be assigned");
        }

        if(ticketExists.getTechnician() != null) {
            throw new TicketAlreadyAssignedException("Ticket already has an assigned technician");
        }

        if(!technicianExists.getRole().equals(UserRole.TECHNICIAN)){
            throw new RoleNotAllowedException("User must be a Technician");
        }

        if(!technicianExists.isActive()){
            throw new UserNotActiveException("Technician is not active");
        }

        ticketExists.setTechnician(technicianExists);
        ticketExists.setStatus(TicketStatus.IN_PROGRESS);
        Ticket savedTicket = ticketRepository.save(ticketExists);
        return ticketMapper.convertToResponseDto(savedTicket);
    }

    @Transactional
    public TicketResponseDto resolve(UUID ticketId, UUID technicianId) {
        Ticket ticketExists = findTicketOrThrow(ticketId);
        User technician = findUserOrThrow(technicianId);

        if(ticketExists.getStatus().equals(TicketStatus.RESOLVED)){
            throw new InvalidTicketStatusException("Ticket already resolved");
        }

        if(!ticketExists.getStatus().equals(TicketStatus.IN_PROGRESS)) {
            throw new InvalidTicketStatusException("Ticket must be In Progress to be resolved");
        }

        if(!technician.getRole().equals(UserRole.TECHNICIAN)) {
            throw new RoleNotAllowedException("User must be a technician to resolve this ticket");
        }

        if(!technician.isActive()){
            throw new UserNotActiveException("Technician is not active");
        }

        if(ticketExists.getTechnician() != null) {
            if(!ticketExists.getTechnician().getId().equals(technician.getId())) {
                throw new TechnicianNotResponsibleException("Technician not responsible for this ticket");
            }
        }

        if(ticketExists.getTechnician() == null) {
            throw new TechnicianNotResponsibleException("Ticket must have a technician to be resolved");
        }

        ticketExists.setStatus(TicketStatus.RESOLVED);
        ticketExists.setResolvedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticketExists);
        return ticketMapper.convertToResponseDto(savedTicket);
    }

    @Transactional
    public TicketResponseDto close(UUID ticketId) {
        Ticket ticketExists = findTicketOrThrow(ticketId);

        if(!ticketExists.getStatus().equals(TicketStatus.RESOLVED)) {
            throw new InvalidTicketStatusException("Ticket must be Resolved to be Closed");
        }

        ticketExists.setStatus(TicketStatus.CLOSED);
        Ticket savedTicket = ticketRepository.save(ticketExists);
        return ticketMapper.convertToResponseDto(savedTicket);
    }
}
