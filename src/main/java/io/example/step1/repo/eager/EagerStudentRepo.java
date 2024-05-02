package io.example.step1.repo.eager;

import io.example.step1.domain.eager.EagerStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EagerStudentRepo extends JpaRepository<EagerStudent, Long> {
}
