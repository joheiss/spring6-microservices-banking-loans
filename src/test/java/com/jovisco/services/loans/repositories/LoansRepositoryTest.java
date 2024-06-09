package com.jovisco.services.loans.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.jovisco.services.loans.constants.LoansConstants;
import com.jovisco.services.loans.entities.Loan;

import jakarta.transaction.Transactional;

@SpringBootTest
public class LoansRepositoryTest {

  @Autowired
  LoansRepository loansRepository;

  String mobileNumber;

  Loan testLoan;

  @BeforeEach
  void setUp() {
    mobileNumber = "+122234567890";
    testLoan = buildLoan();
    loansRepository.save(testLoan);
  }

  @Transactional
  @Rollback
  @Test
  void testFindByMobileNumber() {

    assertTrue(loansRepository.findByMobileNumber(mobileNumber).isPresent());
  }

  @Transactional
  @Rollback
  @Test
  void testFindByMobileNumberNotFound() {

    assertFalse(loansRepository.findByMobileNumber("+122234567899").isPresent());
  }

  @Transactional
  @Rollback
  @Test
  void testFindByLoanNumber() {

    assertTrue(loansRepository.findByLoanNumber(testLoan.getLoanNumber()).isPresent());
  }

  @Transactional
  @Rollback
  @Test
  void testFindByLoanNumberNotFound() {

    var notExistingLoanNumber = "999999999999";
    assertFalse(loansRepository.findByLoanNumber(notExistingLoanNumber).isPresent());
  }

  private Loan buildLoan() {
    return Loan.builder()
        .mobileNumber(mobileNumber)
        .loanNumber("123456789012")
        .loanType(LoansConstants.HOME_LOAN)
        .totalLoan(10000)
        .amountPaid(4000)
        .outstandingAmount(6000)
        .build();
  }
}
