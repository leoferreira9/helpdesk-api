CREATE TABLE email_verification_token(
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    confirmed_at DATETIME NULL,
    created_at DATETIME NOT NULL,

    CONSTRAINT pk_email_verification_token PRIMARY KEY (id),

    CONSTRAINT uk_email_verification_token_user UNIQUE (user_id),

    CONSTRAINT uk_email_verification_token UNIQUE (token_hash),

    CONSTRAINT  fk_email_verification_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);