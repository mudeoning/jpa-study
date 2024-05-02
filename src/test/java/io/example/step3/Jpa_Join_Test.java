package io.example.step3;

import static org.assertj.core.api.Assertions.assertThat;

import io.config.base.JpaTestBase;
import io.example.step3.domain.Company;
import io.example.step3.domain.Employee;
import io.example.step3.repo.CompanyRepo;
import io.example.step3.repo.EmployeeRepo;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 1. Join
 * 2. Fetch join
 * 3. @EntityGraph
 */
@DisplayName("Step3-1: 1:N 관계를 조회하는 Join 동작 비교")
class Jpa_Join_Test extends JpaTestBase {
    private final CompanyRepo companyRepo;
    private final EmployeeRepo employeeRepo;

    public Jpa_Join_Test(CompanyRepo companyRepo, EmployeeRepo employeeRepo) {
        this.companyRepo = companyRepo;
        this.employeeRepo = employeeRepo;
    }

    private Company 네이버, 토스, 카카오 = null;
    private Employee 네이버_직원_1, 토스_직원_1, 토스_직원_2, 카카오_직원_1, 카카오_직원_2, 카카오_직원_3 = null;

    @BeforeEach
    void setUp() {
        네이버 = new Company("네이버");
        토스 = new Company("토스");
        카카오 = new Company("카카오");
        companyRepo.saveAll(List.of(네이버, 토스, 카카오));

        네이버_직원_1 = new Employee("네이버 직원 1", 네이버);

        토스_직원_1 = new Employee("토스 직원 1", 토스);
        토스_직원_2 = new Employee("토스 직원 2", 토스);

        카카오_직원_1 = new Employee("카카오 직원 1", 카카오);
        카카오_직원_2 = new Employee("카카오 직원 2", 카카오);
        카카오_직원_3 = new Employee("카카오 직원 3", 카카오);
        employeeRepo.saveAll(List.of(네이버_직원_1, 토스_직원_1, 토스_직원_2, 카카오_직원_1, 카카오_직원_2, 카카오_직원_3));

        flushAndClear();
    }

    @Nested
    @DisplayName("1. JPQL Join 쿼리를 통해 단건 Company와 해당 Company에 소속된 모든 Employee 조회하면")
    class JpqlJoinTest {
        @Test
        @DisplayName("1. 조회된 Company의 Employee는 초기화 되지 않는다.")
        void isNotInitializedEmployees() {
            // When
            /**
             *      // Join 쿼리를 실행하지만, Join 대상 Entity 조회는 미 관여
             *     select
             *         c1_0.id,
             *         c1_0.name
             *     from
             *         company c1_0
             *     join
             *         employee e1_0
             *             on c1_0.id=e1_0.company_id
             *     where
             *         c1_0.id=?
             */
            Company actual = companyRepo.findCompanyByIdByJpqlJoin(네이버.getId());

            // Then
            assertThat(Hibernate.isInitialized(actual.getEmployees()))
                .as("JPQL Join 쿼리를 통해 조회된 Company의 Employee 초기화 여부 검증")
                .as("일반 join은 실제 join 쿼리가 실행 되지만 join 대상에 대한 영속성까지는 미 관여")
                .isFalse();
        }

        @Test
        @DisplayName("2. Company의 Employee를 초기화 하기 위해서는 객체 접근이 필요하다.")
        void needToAccessEmployeesForInitialize() {
            // When
            Company actual = companyRepo.findCompanyByIdByJpqlJoin(네이버.getId());

            printLine();
            actual.getEmployees().size();

            // Then
            assertThat(Hibernate.isInitialized(actual.getEmployees()))
                .as("JPQL Join 쿼리를 통해 조회된 Company의 Employee에 접근 후 초기화 여부 검증")
                .isTrue();
        }

        @Test
        @DisplayName("3. 명시적인 Join 호출이지만, 연관관계는 select 대상에 포함되지 않음에 주의 해야 한다.")
        void dd() {
            /**
             *     select
             *         c1_0.id,
             *         c1_0.name
             *     from
             *         company c1_0
             *     join
             *         employee e1_0
             *             on c1_0.id=e1_0.company_id
             *     where
             *         c1_0.id=?
             * ============================================== // printLine();
             *     // proxy 객체 초기화를 위해 실행되는 조회 쿼리
             *     select
             *         e1_0.company_id,
             *         e1_0.id,
             *         e1_0.name
             *     from
             *         employee e1_0
             *     where
             *         e1_0.company_id=?
             */
            // 명시적 Join 호출이지만 Company만 조회되고, 연관된 Employee는 조회되지 않으므로,
            Company actual = companyRepo.findCompanyByIdByJpqlJoin(네이버.getId());

            printLine();
            // Employee 사용을 위해 객체에 접근하는 과정에서 초기화를 위한 조회 쿼리가 의도치 않게 발생할 수 있음에 주의
            actual.getEmployees().size();
        }
    }

    @Nested
    @DisplayName("2. JPQL Fetch Join 쿼리를 통해 단건 Company와 해당 Company에 소속된 모든 Employee 조회하면")
    class JpqlFetchJoinTest {
        @Test
        @DisplayName("1. Company와 Company의 Employee 모두 조회된다.")
        void initializedEmployees() {
            // When
            Company actual = companyRepo.findCompanyByIdByJpqlFetchJoin(네이버.getId());

            // Then
            assertThat(Hibernate.isInitialized(actual.getEmployees())).isTrue();
        }

        @Test
        @DisplayName("2. 쿼리 실행 이후, Company의 Employee에 접근해도 초기화를 위한 쿼리가 실행 되지 않는다.")
        void isNotNeedToAccessEmployeesForInitialize() {
            // When
            /**
             *     // Join 대상 Entity를 한번에 조회
             *     select
             *         c1_0.id,
             *         e1_0.company_id,
             *         e1_0.id,
             *         e1_0.name,
             *         c1_0.name
             *     from
             *         company c1_0
             *     join
             *         employee e1_0
             *             on c1_0.id=e1_0.company_id
             *     where
             *         c1_0.id=?
             */
            Company actual = companyRepo.findCompanyByIdByJpqlFetchJoin(네이버.getId());
            printLine();
            actual.getEmployees().size();

            // Then
            assertThat(Hibernate.isInitialized(actual.getEmployees())).isTrue();
        }
    }

    @Nested
    @DisplayName("3. @EntityGraph를 통해 단건 Company와 해당 Company에 소속된 모든 Employee 조회하면")
    class EntityGraphTest {
        @Test
        @DisplayName("1. Company와 Company의 Employee 모두 조회된다.")
        void initializedEmployees() {
            // When
            Company actual = companyRepo.findCompanyById(네이버.getId());

            // Then
            assertThat(Hibernate.isInitialized(actual.getEmployees())).isTrue();
        }

        @Test
        @DisplayName("2. 쿼리 실행 이후, Company의 Employee에 접근해도 초기화를 위한 쿼리가 실행 되지 않는다.")
        void isNotNeedToAccessEmployeesForInitialize() {
            // When
            /**
             *     select
             *         c1_0.id,
             *         e1_0.company_id,
             *         e1_0.id,
             *         e1_0.name,
             *         c1_0.name
             *     from
             *         company c1_0
             *     left join
             *         employee e1_0
             *             on c1_0.id=e1_0.company_id
             *     where
             *         c1_0.id=?
             */
            Company actual = companyRepo.findCompanyById(네이버.getId());
            printLine();
            actual.getEmployees().size();

            // Then
            assertThat(Hibernate.isInitialized(actual.getEmployees())).isTrue();
        }
    }
}
