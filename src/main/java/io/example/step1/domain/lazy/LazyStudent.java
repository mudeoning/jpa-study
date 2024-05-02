package io.example.step1.domain.lazy;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LazyStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "group_id",
        foreignKey = @ForeignKey(name = "FK_STUDENT_LAZY_TO_GROUP_LAZY")
    )
    private LazyGroup lazyGroup;

    public LazyStudent(String name, LazyGroup lazyGroup) {
        this.name = name;
        this.lazyGroup = lazyGroup;
        lazyGroup.addStudent(this);
    }
}
