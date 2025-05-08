package com.cinema.repository;

import com.cinema.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Query(value =
        "SELECT * FROM movies m WHERE " +
        "(:title IS NULL OR m.title ILIKE CONCAT('%', CAST(:title AS text), '%')) AND " +
        "(:genre IS NULL OR m.genre = :genre) AND " +
        "(:isShowing IS NULL OR m.is_showing = :isShowing) AND " +
        "(:fromDate IS NULL OR m.release_date >= :fromDate) AND " +
        "(:toDate IS NULL OR m.release_date <= :toDate)",
        nativeQuery = true)
    List<Movie> searchMovies(
        @Param("title") String title,
        @Param("genre") String genre,
        @Param("isShowing") Boolean isShowing,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}