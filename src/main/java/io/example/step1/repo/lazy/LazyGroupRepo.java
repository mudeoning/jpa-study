package io.example.step1.repo.lazy;

import io.example.step1.domain.lazy.LazyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LazyGroupRepo extends JpaRepository<LazyGroup, Long> {
}
