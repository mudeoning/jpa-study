package io.example.step1.repo.eager;

import io.example.step1.domain.eager.EagerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EagerGroupRepo extends JpaRepository<EagerGroup, Long> {
}
