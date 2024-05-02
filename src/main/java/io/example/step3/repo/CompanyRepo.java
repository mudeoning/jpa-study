package io.example.step3.repo;

import io.example.step3.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepo extends JpaRepository<Company, Long> {

    @Query("select c "
        + "from Company as c "
        + "join c.employees as e "
        + "where c.id = :id")
    Company findCompanyByIdByJpqlJoin(@Param("id") Long companyId);

    @Query("select c "
        + "from Company as c "
        + "join fetch c.employees as e "
        + "where c.id = :id")
    Company findCompanyByIdByJpqlFetchJoin(@Param("id") Long companyId);

    @EntityGraph(attributePaths = "employees")
    Company findCompanyById(Long companyId);

    @Query(value = "select c "
        + "from Company as c "
        + "join fetch c.employees as e ",
        countQuery = "select count(DISTINCT c) FROM Company as c INNER JOIN c.employees")
    Page<Company> findCompanyByFetchJoinPaging(Pageable pageable);

    @Query("select c "
        + "from Company as c "
        + "join fetch c.employees as e "
        + "join fetch c.offices as o "
        + "where c.id = :id")
    Company findCompanyByFetchJoinWithAllRelational(@Param("id") Long companyId);

    @EntityGraph(attributePaths = {"employees", "offices"})
    Company findEntityCompanyById(Long companyId);

    @EntityGraph(attributePaths = {"employees"})
    Page<Company> findAll(Pageable pageable);
}
