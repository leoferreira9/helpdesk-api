CREATE TABLE ticket_comment(
    id BINARY(16) NOT NULL,
    ticket_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    comment VARCHAR(200) NOT NULL,
    created_at DATETIME NOT NULL,

    CONSTRAINT pk_ticket_comment_id PRIMARY KEY (id),

    CONSTRAINT fk_ticket_comment_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id),

    CONSTRAINT fk_ticket_comment_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);