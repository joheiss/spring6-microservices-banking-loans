package com.jovisco.services.loans.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jovisco.services.loans.entities.Loan;

@Repository
public interface LoansRepository extends JpaRepository<Loan, Long> {

  Optional<Loan> findByMobileNumber(String mobileNumber);

  Optional<Loan> findByLoanNumber(String loanNumber);
}
