package io.example.step3.repo;

import io.example.step3.domain.Office;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficeRepo extends JpaRepository<Office, Long> {
}
