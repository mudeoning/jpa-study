package io.example.step3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.config.base.JpaTestBase;
import io.example.step3.domain.Company;
import io.example.step3.domain.Employee;
import io.example.step3.domain.Office;
import io.example.step3.repo.CompanyRepo;
import io.example.step3.repo.EmployeeRepo;
import io.example.step3.repo.OfficeRepo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("Step3-2: Join의 한계")
public class Limit_Of_Join extends JpaTestBase {
    private final CompanyRepo companyRepo;
    private final EmployeeRepo employeeRepo;
    private final OfficeRepo officeRepo;

    public Limit_Of_Join(CompanyRepo companyRepo, EmployeeRepo employeeRepo, OfficeRepo officeRepo) {
        this.companyRepo = companyRepo;
        this.employeeRepo = employeeRepo;
        this.officeRepo = officeRepo;
    }

    private Company 네이버, 토스, 카카오 = null;
    private Employee 네이버_직원_1, 토스_직원_1, 토스_직원_2, 카카오_직원_1, 카카오_직원_2, 카카오_직원_3 = null;
    private Office 네이버_사옥;

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

        네이버_사옥 = new Office("1784", 네이버);
        officeRepo.save(네이버_사옥);

        flushAndClear();
    }

    @Nested
    @DisplayName("[Case#1] FetchJoin은")
    class fetchJoinProblems {
        // 목록 조회 -> 페이징 조회
        @Test
        @DisplayName("1. 모든 데이터 조회 후 어플리케이션 메모리상에서 페이징 처리 한다.")
        void PaginationInApplicationMemory() {
            // Given
            Pageable pageable = PageRequest.of(0, 1);
            /**
             *     firstResult/maxResults specified with collection fetch; applying in memory
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
             *      // limit, offset을 이용한 부분조회 조건 없이 모든 데이터를 조회
             * [Hibernate]
             *     select
             *         count(distinct c1_0.id)
             *     from
             *         company c1_0
             *     join
             *         employee e1_0
             *             on c1_0.id=e1_0.company_id
             */
            // Then
            Page<Company> actual = companyRepo.findCompanyByFetchJoinPaging(pageable);
            /**
             * 실제로 페이징 처리는 오류 없이 잘 수행되었지만,
             * firstResult/maxResults specified with collection fetch; applying in memory 로그에 따라
             * 모든 데이터를 메모리에 적재 후, 메모리에서 페이징 처리를 진행함에 주의
             */
            assertThat(actual).hasSize(1);
            assertThat(actual.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("2. 1개 이상의 N관계 조회 시 MultipleBagFetchException이 발생한다.")
        void throwInvalidDataAccessApiUsageException() {
            // When & Then
            assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
                .isThrownBy(() -> companyRepo.findCompanyByFetchJoinWithAllRelational(네이버.getId()));
        }
    }

    @Nested
    @DisplayName("[Case#2] @EntityGraph는")
    class EntityGraphProblems {
        @Test
        @DisplayName("1. 모든 데이터 조회 후 어플리케이션 메모리상에서 페이징 처리 한다.")
        void PaginationInApplicationMemory() {
            // Given
            Pageable pageable = PageRequest.of(0, 1);

            // When
            /**
             *     firstResult/maxResults specified with collection fetch; applying in memory
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
             * [Hibernate]
             *     select
             *         count(c1_0.id)
             *     from
             *         company c1_0
             */
            Page<Company> actual = companyRepo.findAll(pageable);

            // Then
            assertThat(actual).hasSize(1);
            assertThat(actual.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("2. 1개 이상의 N관계 조회 시 MultipleBagFetchException이 발생한다.")
        void throwInvalidDataAccessApiUsageException() {
            // When & Then
            assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
                .isThrownBy(() -> companyRepo.findEntityCompanyById(네이버.getId()));
        }
    }
}
