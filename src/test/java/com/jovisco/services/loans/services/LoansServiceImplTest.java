package com.jovisco.services.loans.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.jovisco.services.loans.constants.LoansConstants;
import com.jovisco.services.loans.dtos.CreateLoanDto;
import com.jovisco.services.loans.entities.Loan;
import com.jovisco.services.loans.exceptions.LoanAlreadyExistsException;
import com.jovisco.services.loans.exceptions.ResourceNotFoundException;
import com.jovisco.services.loans.mappers.LoanMapper;
import com.jovisco.services.loans.repositories.LoansRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
public class LoansServiceImplTest {

  @Autowired
  LoansService loansService;

  @Autowired
  LoansRepository loansRepository;

  Loan testLoan;

  @BeforeEach
  void setUp() {
    testLoan = loansRepository.save(buildLoan());
  }

  @Transactional
  @Rollback
  @Test
  void testCreateLoan() {

    // create new loan - with other mobile number than the already existing loan
    var createDto = new CreateLoanDto("+233345678901");
    loansService.createLoan(createDto);

    // check if loan has been created
    assertTrue(loansRepository.findByMobileNumber(createDto.getMobileNumber()).isPresent());
  }

  @Transactional
  @Rollback
  @Test
  void testCreateLoanWithAlreadyExistsError() {

    // try to create new loan - with same mobile number than the already existing loan
    var createDto = new CreateLoanDto(testLoan.getMobileNumber());

    // check that exception is thrown
    assertThatExceptionOfType(LoanAlreadyExistsException.class)
        .isThrownBy(() -> loansService.createLoan(createDto));

  }

  @Transactional
  @Rollback
  @Test
  void testDeleteLoan() {
    // test if delete works
    assertTrue(loansService.deleteLoan(testLoan.getMobileNumber()));

    // test if loan is really deleted from database
    assertFalse(loansRepository.findByMobileNumber(testLoan.getMobileNumber()).isPresent());
  }

  @Transactional
  @Rollback
  @Test
  void testDeleteLoanWithNotFoundError() {

    // first delete test loan
    loansService.deleteLoan(testLoan.getMobileNumber());

    // check that exception is thrown
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> loansService.deleteLoan(testLoan.getMobileNumber()));

  }

  @Transactional
  @Rollback
  @Test
  void testFetchLoan() {

    var loanDto = loansService.fetchLoan(testLoan.getMobileNumber());

    assertThat(loanDto).isNotNull();
    assertThat(loanDto.getMobileNumber()).isEqualTo(testLoan.getMobileNumber());
  }

  @Transactional
  @Rollback
  @Test
  void testFetchLoanWithNotFoundError() {

    // first delete test loan
    loansService.deleteLoan(testLoan.getMobileNumber());

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> loansService.fetchLoan(testLoan.getMobileNumber()));
  }

  @Transactional
  @Rollback
  @Test
  void testUpdateLoan() {

    // change some loan data
    var loanDto = LoanMapper.mapToLoanDto(testLoan);
    loanDto.setTotalLoan(33333);
    loanDto.setAmountPaid(22222);
    loanDto.setOutstandingAmount(11111);

    assertTrue(loansService.updateLoan(loanDto));

    // check for correctly updated values
    var optionalLoan = loansRepository.findByMobileNumber(loanDto.getMobileNumber());
    assertTrue(optionalLoan.isPresent());
    var loan = optionalLoan.get();
    assertTrue(loan.getTotalLoan() == loanDto.getTotalLoan() &&
        loan.getAmountPaid() == loanDto.getAmountPaid() &&
        loan.getOutstandingAmount() == loanDto.getOutstandingAmount());
  }

  @Transactional
  @Rollback
  @Test
  void testUpdateLoanWithNotFoundError() {

    // change some loan data
    var loanDto = LoanMapper.mapToLoanDto(testLoan);
    loanDto.setTotalLoan(33333);
    loanDto.setLoanNumber("DOESNOTEXIST");

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> loansService.updateLoan(loanDto));
  }

  private Loan buildLoan() {
    return Loan.builder()
        .mobileNumber("+122234567890")
        .loanNumber("123456789012")
        .loanType(LoansConstants.HOME_LOAN)
        .totalLoan(10000)
        .amountPaid(4000)
        .outstandingAmount(6000)
        .build();
  }
}
