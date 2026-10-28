package com.portal.repository;

import com.portal.model.Company;
import com.portal.model.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<Company> findByApprovalStatus(CompanyStatus status);
    List<Company> findByApprovalStatusOrderByNameAsc(CompanyStatus status);
    long countByApprovalStatus(CompanyStatus status);
}
