package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.dto.update.TicketDetailsUpdateDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.ResourceNotFoundException;
import com.leonardo.helpdesk.exception.RoleNotAllowedException;
import com.leonardo.helpdesk.exception.UserNotActiveException;
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
    public TicketResponseDto findById(UUID id){
        Ticket ticketExists = findTicketOrThrow(id);
        return ticketMapper.convertToResponseDto(ticketExists);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponseDto> findAll(Pageable pageable) {
        Page<Ticket> page = ticketRepository.findAll(pageable);
        return page.map(ticketMapper::convertToResponseDto);
    }

    @Transactional
    public TicketResponseDto updateDetails(UUID id, TicketDetailsUpdateDto detailsUpdateDto) {
        Ticket ticketExists = findTicketOrThrow(id);

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
    public TicketResponseDto assignTechnician(UUID id, UUID technicianId) {
        Ticket ticketExists = findTicketOrThrow(id);
        User technicianExists = findUserOrThrow(technicianId);

        if(!technicianExists.getRole().equals(UserRole.TECHNICIAN)){
            throw new RoleNotAllowedException("User must be a Technician");
        }

        if(!technicianExists.isActive()){
            throw new UserNotActiveException("Technician is not active");
        }

        ticketExists.setTechnician(technicianExists);
        Ticket savedTicket = ticketRepository.save(ticketExists);
        return ticketMapper.convertToResponseDto(savedTicket);
    }

    @Transactional
    public TicketResponseDto updateStatus(UUID id, TicketStatus status) {
        Ticket ticketExists = findTicketOrThrow(id);
        ticketExists.setStatus(status);
        Ticket savedTicket = ticketRepository.save(ticketExists);
        return ticketMapper.convertToResponseDto(savedTicket);
    }

    @Transactional
    public TicketResponseDto close(UUID id) {
        Ticket ticketExists = findTicketOrThrow(id);
        ticketExists.setStatus(TicketStatus.CLOSED);
        ticketExists.setResolvedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticketExists);
        return ticketMapper.convertToResponseDto(savedTicket);
    }
}
