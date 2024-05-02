package io.example.step1;

import static org.assertj.core.api.Assertions.assertThat;

import io.config.base.JpaTestBase;
import io.example.step1.domain.eager.EagerGroup;
import io.example.step1.domain.eager.EagerStudent;
import io.example.step1.domain.lazy.LazyGroup;
import io.example.step1.domain.lazy.LazyStudent;
import io.example.step1.repo.eager.EagerGroupRepo;
import io.example.step1.repo.eager.EagerStudentRepo;
import io.example.step1.repo.lazy.LazyGroupRepo;
import io.example.step1.repo.lazy.LazyStudentRepo;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Step1: Fetch Type EAGER vs LAZY 동작 살펴보기")
public class Fetch_Type_Eager_And_Lazy_Test extends JpaTestBase {
    private final LazyGroupRepo lazyGroupRepo;
    private final LazyStudentRepo lazyStudentRepo;

    private final EagerGroupRepo eagerGroupRepo;
    private final EagerStudentRepo eagerStudentRepo;

    public Fetch_Type_Eager_And_Lazy_Test(
        LazyGroupRepo lazyGroupRepo,
        LazyStudentRepo lazyStudentRepo,
        EagerGroupRepo eagerGroupRepo,
        EagerStudentRepo eagerStudentRepo
    ) {
        this.lazyGroupRepo = lazyGroupRepo;
        this.lazyStudentRepo = lazyStudentRepo;
        this.eagerGroupRepo = eagerGroupRepo;
        this.eagerStudentRepo = eagerStudentRepo;
    }

    private LazyGroup lazyGroup = null;
    private LazyStudent lazyStudent = null;
    private EagerGroup eagerGroup = null;
    private EagerStudent eagerStudent = null;

    @BeforeEach
    void setUp() {
        lazyGroup = new LazyGroup("Group Lazy");
        lazyStudent = new LazyStudent("Student Lazy", lazyGroup);
        lazyGroupRepo.save(lazyGroup);
        lazyStudentRepo.save(lazyStudent);

        eagerGroup = new EagerGroup("Group Eager");
        eagerStudent = new EagerStudent("Student Eager", eagerGroup);
        eagerGroupRepo.save(eagerGroup);
        eagerStudentRepo.save(eagerStudent);

        flushAndClear();
    }

    @Nested
    @DisplayName("FetchType.EAGER 전략은")
    class FetchTypeEagerTest {
        @Test
        @DisplayName("1. Group 조회 시, Group과 연관관계를 가지는 Student를 즉시 로딩한다.")
        void fetchTypeEager() {
            // When
            /**
             *     // Eager 전략으로 인해 연관된 Stdudent를 조회하기 위해 left outer join 쿼리 실행
             *     select
             *         eg1_0.id,
             *         eg1_0.name,
             *         s1_0.group_id,
             *         s1_0.id,
             *         s1_0.name
             *     from
             *         eager_group eg1_0
             *     left join
             *         eager_student s1_0
             *             on eg1_0.id=s1_0.group_id
             *     where
             *         eg1_0.id=?
             */
            EagerGroup actual = eagerGroupRepo.findById(eagerGroup.getId()).orElseThrow();

            // Then
            assertThat(actual).isNotNull();
            assertThat(actual.getStudents()).isNotNull();
        }

        @Test
        @DisplayName("2. Group만 조회 하고 싶은 경우, 연관 관계를 가지는 Entity가 left outer join으로 즉시 조회 되는 현상에 주의")
        void dd() {
            // Given
            EagerGroup actual = eagerGroupRepo.findById(eagerGroup.getId()).orElseThrow();

            // When

            // Then
        }
    }

    @Nested
    @DisplayName("FetchType.LAZY 전략은")
    class FetchTypeLazyTest {
        @Test
        @DisplayName("1. Group 조회 시, 연관관계를 가지는 Student를 즉시 로딩하지 않는다.")
        void fetchTypeLazy() {
            // When
            /**
             *     // Lazy 전략으로 인해 연관된 Student를 조회 하지 않음
             *     select
             *         lg1_0.id,
             *         lg1_0.name
             *     from
             *         lazy_group lg1_0
             *     where
             *         lg1_0.id=?
             */
            LazyGroup actual = lazyGroupRepo.findById(lazyGroup.getId()).orElseThrow();

            // Then
            assertThat(actual).isNotNull();
        }

        @Test
        @DisplayName("연관관계를 가지는 Entity의 값을 Null로 설정할까?")
        void proxy() {
            // When
            LazyGroup actual = lazyGroupRepo.findById(lazyGroup.getId()).orElseThrow();

            // Then
            assertThat(actual).isNotNull();
            assertThat(actual.getStudents()).isNotNull();
            assertThat(Hibernate.isInitialized(actual.getStudents())).isFalse();
        }
    }
}
