package com.cinema.repository;

import com.cinema.model.Movie;
import com.cinema.model.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByStatus(MovieStatus status);
    List<Movie> findByStatusIn(List<MovieStatus> statuses);
    List<Movie> findByGenreAndStatus(String genre, MovieStatus status);

    @Query(value =
        "SELECT * FROM movies m WHERE " +
        "(:title IS NULL OR m.title ILIKE '%' || :title || '%') AND " +
        "(:genre IS NULL OR m.genre = :genre) AND " +
        "(:status IS NULL OR m.status = CAST(:status as varchar)) AND " +
        "(m.release_date >= COALESCE(CAST(:fromDate AS DATE), m.release_date)) AND " +
        "(m.release_date <= COALESCE(CAST(:toDate AS DATE), m.release_date))",
        nativeQuery = true)
    List<Movie> searchMovies(
        @Param("title") String title,
        @Param("genre") String genre,
        @Param("status") String status,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
