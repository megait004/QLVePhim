-- Insert sample users
INSERT INTO users (username, email, password, full_name, phone_number)
VALUES
('admin', 'admin@cinema.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'Admin User', '0123456789'),
('user', 'user@cinema.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'Normal User', '0987654321');

-- Insert user roles
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin'
UNION ALL
SELECT id, 'ROLE_USER' FROM users WHERE username = 'user';

-- Insert sample movies
INSERT INTO movies (title, description, genre, duration, release_date, director, movie_cast, poster_url, is_showing)
VALUES
('Avengers: Endgame', 'After the devastating events of Infinity War, the universe is in ruins. With the help of remaining allies, the Avengers assemble once more in order to reverse Thanos actions and restore balance to the universe.', 'Action', 181, '2019-04-26', 'Russo Brothers', 'Robert Downey Jr., Chris Evans, Mark Ruffalo, Chris Hemsworth', 'https://example.com/endgame.jpg', true),
('The Dark Knight', 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.', 'Action', 152, '2008-07-18', 'Christopher Nolan', 'Christian Bale, Heath Ledger, Aaron Eckhart, Michael Caine', 'https://example.com/dark-knight.jpg', true),
('Inception', 'A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.', 'Sci-Fi', 148, '2010-07-16', 'Christopher Nolan', 'Leonardo DiCaprio, Joseph Gordon-Levitt, Ellen Page', 'https://example.com/inception.jpg', true),
('Interstellar', 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity survival.', 'Sci-Fi', 169, '2014-11-07', 'Christopher Nolan', 'Matthew McConaughey, Anne Hathaway, Jessica Chastain', 'https://example.com/interstellar.jpg', true),
('The Matrix', 'A computer programmer discovers that reality as he knows it is a simulation created by machines, and joins a rebellion to break free.', 'Sci-Fi', 136, '1999-03-31', 'Wachowski Sisters', 'Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss', 'https://example.com/matrix.jpg', true);

-- Insert screenings for next 7 days
INSERT INTO screenings (movie_id, start_time, hall_number, price, available_seats)
SELECT
    m.id,
    CURRENT_TIMESTAMP + (n || ' days')::interval + (h || ' hours')::interval,
    'Hall ' || hall,
    CASE
        WHEN m.genre = 'Action' THEN 15.00
        WHEN m.genre = 'Sci-Fi' THEN 12.50
        ELSE 10.00
    END,
    CASE
        WHEN hall = 'A' THEN 100
        WHEN hall = 'B' THEN 80
        ELSE 120
    END
FROM movies m
CROSS JOIN generate_series(1, 7) n
CROSS JOIN generate_series(10, 22, 3) h
CROSS JOIN (VALUES ('A'), ('B'), ('C')) halls(hall)
WHERE m.is_showing = true;

-- Insert sample tickets
INSERT INTO tickets (user_id, screening_id, seat_number, booking_time, price, status, qr_code)
SELECT
    u.id,
    s.id,
    CASE (random() * 3)::int
        WHEN 0 THEN 'A'
        WHEN 1 THEN 'B'
        ELSE 'C'
    END || generate_series(1, 5),
    s.start_time - interval '2 days',
    s.price,
    'BOOKED',
    'QR_' || u.id || '_' || s.id || '_' || generate_series(1, 5)
FROM users u
CROSS JOIN (
    SELECT id, start_time, price
    FROM screenings
    WHERE start_time > CURRENT_TIMESTAMP
    LIMIT 3
) s
WHERE u.username = 'user';