package io.example.step1.domain.lazy;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 1 Group <-> N Student
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LazyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "lazyGroup", fetch = FetchType.LAZY)
    private List<LazyStudent> students = new ArrayList<>();

    public LazyGroup(String name) {
        this.name = name;
    }

    public void addStudent(LazyStudent lazyStudent) {
        students.add(lazyStudent);
    }
}
