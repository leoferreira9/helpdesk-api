package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketPriority;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.repository.TicketRepository;
import com.leonardo.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class TicketServiceIntegrationTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketService ticketService;

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");



    @Transactional
    @Test
    void shouldReturnOnlyTicketsRequestedByAuthenticatedUser() {
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setName("User 1 name");
        user1.setEmail("User1@email.com");
        user1.setPassword("test123");
        user1.setRole(UserRole.USER);
        user1.setActive(true);

        User user2 = new User();
        user2.setName("User 2 name");
        user2.setEmail("User2@email.com");
        user2.setPassword("test123");
        user2.setRole(UserRole.USER);
        user2.setActive(true);

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        Ticket ticket1 = new Ticket();
        ticket1.setRequester(savedUser1);
        ticket1.setTitle("Test 1");
        ticket1.setDescription("Description 1");
        ticket1.setPriority(TicketPriority.LOW);

        Ticket ticket2 = new Ticket();
        ticket2.setRequester(savedUser2);
        ticket2.setTitle("Test 2");
        ticket2.setDescription("Description 2");
        ticket2.setPriority(TicketPriority.LOW);

        Ticket savedTicket1 = ticketRepository.save(ticket1);
        Ticket savedTicket2 = ticketRepository.save(ticket2);

        Page<TicketResponseDto> result = ticketService.findAll(savedUser1, pageable, null, null, null, null, null, null);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("User 1 name", result.getContent().getFirst().requesterName());
        Assertions.assertEquals("User1@email.com", result.getContent().getFirst().requesterEmail());
        Assertions.assertEquals("Test 1", result.getContent().getFirst().title());
        Assertions.assertEquals("Description 1", result.getContent().getFirst().description());
        Assertions.assertEquals(savedTicket1.getId(), result.getContent().getFirst().id());
        Assertions.assertNotEquals(savedTicket2.getId(), result.getContent().getFirst().id());
    }

    @Transactional
    @Test
    void shouldReturnOnlyTicketsAssignedToAuthenticatedTechnician() {
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setName("User 1 name");
        user1.setEmail("User1@email.com");
        user1.setPassword("test123");
        user1.setRole(UserRole.USER);
        user1.setActive(true);

        User user2 = new User();
        user2.setName("User 2 name");
        user2.setEmail("User2@email.com");
        user2.setPassword("test123");
        user2.setRole(UserRole.USER);
        user2.setActive(true);

        User technician1 = new User();
        technician1.setName("Technician 1");
        technician1.setEmail("Technician1@email.com");
        technician1.setPassword("test123");
        technician1.setRole(UserRole.TECHNICIAN);
        technician1.setActive(true);

        User technician2 = new User();
        technician2.setName("Technician 2");
        technician2.setEmail("Technician2@email.com");
        technician2.setPassword("test123");
        technician2.setRole(UserRole.TECHNICIAN);
        technician2.setActive(true);

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);
        userRepository.save(technician1);
        userRepository.save(technician2);

        Ticket ticket1 = new Ticket();
        ticket1.setRequester(savedUser1);
        ticket1.setTitle("Test 1");
        ticket1.setDescription("Description 1");
        ticket1.setPriority(TicketPriority.LOW);
        ticket1.setTechnician(technician1);

        Ticket ticket2 = new Ticket();
        ticket2.setRequester(savedUser2);
        ticket2.setTitle("Test 2");
        ticket2.setDescription("Description 2");
        ticket2.setPriority(TicketPriority.LOW);
        ticket2.setTechnician(technician2);

        Ticket savedTicket1 = ticketRepository.save(ticket1);
        Ticket savedTicket2 = ticketRepository.save(ticket2);

        Page<TicketResponseDto> result = ticketService.findAll(technician1, pageable, null, null, null, null, null, null);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("User 1 name", result.getContent().getFirst().requesterName());
        Assertions.assertEquals("User1@email.com", result.getContent().getFirst().requesterEmail());
        Assertions.assertEquals("Test 1", result.getContent().getFirst().title());
        Assertions.assertEquals("Description 1", result.getContent().getFirst().description());
        Assertions.assertEquals(savedTicket1.getId(), result.getContent().getFirst().id());
        Assertions.assertNotEquals(savedTicket2.getId(), result.getContent().getFirst().id());
        Assertions.assertEquals("Technician 1", result.getContent().getFirst().technicianName());
        Assertions.assertEquals("Technician1@email.com", result.getContent().getFirst().technicianEmail());
        Assertions.assertNotEquals("Technician 2", result.getContent().getFirst().technicianName());
        Assertions.assertNotEquals("Technician2@email.com", result.getContent().getFirst().technicianEmail());
    }

    @Transactional
    @Test
    void shouldUseProvidedFiltersWhenAuthenticatedUserIsAdmin() {
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setName("User 1 name");
        user1.setEmail("User1@email.com");
        user1.setPassword("test123");
        user1.setRole(UserRole.USER);
        user1.setActive(true);

        User user2 = new User();
        user2.setName("User 2 name");
        user2.setEmail("User2@email.com");
        user2.setPassword("test123");
        user2.setRole(UserRole.USER);
        user2.setActive(true);

        User technician1 = new User();
        technician1.setName("Technician 1");
        technician1.setEmail("Technician1@email.com");
        technician1.setPassword("test123");
        technician1.setRole(UserRole.TECHNICIAN);
        technician1.setActive(true);

        User technician2 = new User();
        technician2.setName("Technician 2");
        technician2.setEmail("Technician2@email.com");
        technician2.setPassword("test123");
        technician2.setRole(UserRole.TECHNICIAN);
        technician2.setActive(true);

        User admin = new User();
        admin.setName("admin");
        admin.setEmail("admin@email.com");
        admin.setPassword("admin123");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);
        User savedAdmin = userRepository.save(admin);
        User savedTechnician1 = userRepository.save(technician1);
        User savedTechnician2 = userRepository.save(technician2);

        Ticket ticket1 = new Ticket();
        ticket1.setRequester(savedUser1);
        ticket1.setTitle("Test 1");
        ticket1.setDescription("Description 1");
        ticket1.setPriority(TicketPriority.LOW);
        ticket1.setTechnician(savedTechnician1);

        Ticket ticket2 = new Ticket();
        ticket2.setRequester(savedUser2);
        ticket2.setTitle("Test 2");
        ticket2.setDescription("Description 2");
        ticket2.setPriority(TicketPriority.LOW);
        ticket2.setTechnician(savedTechnician2);

        Ticket savedTicket1 = ticketRepository.save(ticket1);
        Ticket savedTicket2 = ticketRepository.save(ticket2);

        Page<TicketResponseDto> result = ticketService.findAll(
                savedAdmin,
                pageable,
                null,
                null,
                savedUser2.getId(),
                savedTechnician2.getId(),
                null,
                null
        );

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(savedTicket2.getId(), result.getContent().getFirst().id());
        Assertions.assertNotEquals(savedTicket1.getId(), result.getContent().getFirst().id());
        Assertions.assertEquals("User 2 name", result.getContent().getFirst().requesterName());
        Assertions.assertEquals("Technician 2", result.getContent().getFirst().technicianName());
    }
}
