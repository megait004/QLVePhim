CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    director VARCHAR(255) NOT NULL,
    movie_cast VARCHAR(255),
    duration INTEGER NOT NULL,
    genre VARCHAR(255) NOT NULL,
    language VARCHAR(255),
    rating VARCHAR(255),
    trailer_url VARCHAR(255),
    poster_url VARCHAR(255) NOT NULL,
    release_date TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'COMING_SOON',
    CONSTRAINT check_status CHECK (status IN ('NOW_SHOWING', 'COMING_SOON', 'HOT', 'ENDED'))
);

CREATE TABLE screenings (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    hall_number VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    available_seats INTEGER NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    screening_id BIGINT NOT NULL,
    seat_number VARCHAR(255) NOT NULL,
    booking_time TIMESTAMP NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'BOOKED',
    qr_code TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (screening_id) REFERENCES screenings(id)
);