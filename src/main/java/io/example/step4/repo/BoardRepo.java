package io.example.step4.repo;

import io.example.step4.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepo extends JpaRepository<Board, Long> {

}
