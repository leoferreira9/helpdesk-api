CREATE TABLE tickets (
    id BINARY(16) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(30) NOT NULL,

    requester_id BINARY(16) NOT NULL,
    technician_id BINARY(16) NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    resolved_at DATETIME NULL,

    CONSTRAINT pk_tickets PRIMARY KEY (id),

    CONSTRAINT fk_tickets_requester
        FOREIGN KEY (requester_id)
        REFERENCES users(id),

    CONSTRAINT fk_tickets_technician
        FOREIGN KEY (technician_id)
        REFERENCES users(id)
);