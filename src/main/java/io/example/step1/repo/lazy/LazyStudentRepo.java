package io.example.step1.repo.lazy;

import io.example.step1.domain.lazy.LazyStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LazyStudentRepo extends JpaRepository<LazyStudent, Long> {
}
