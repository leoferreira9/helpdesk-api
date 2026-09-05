CREATE TABLE ticket_history(
    id BINARY(16) NOT NULL,
    ticket_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    action VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    created_at DATETIME NOT NULL,

    CONSTRAINT pk_ticket_history PRIMARY KEY (id),

    CONSTRAINT fk_ticket_history_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id),

    CONSTRAINT fk_ticket_history_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);