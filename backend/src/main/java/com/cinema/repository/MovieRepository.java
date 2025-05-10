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
    "(:title IS NULL OR m.title ILIKE '%' || :title || '%') AND " +
    "(:genre IS NULL OR m.genre = :genre) AND " +
    "(:isShowing IS NULL OR m.is_showing = :isShowing) AND " +
    "(m.release_date >= COALESCE(CAST(:fromDate AS DATE), m.release_date)) AND " +
    "(m.release_date <= COALESCE(CAST(:toDate AS DATE), m.release_date))",
    nativeQuery = true)
List<Movie> searchMovies(
    @Param("title") String title,
    @Param("genre") String genre,
    @Param("isShowing") Boolean isShowing,
    @Param("fromDate") LocalDate fromDate,
    @Param("toDate") LocalDate toDate
);

}
