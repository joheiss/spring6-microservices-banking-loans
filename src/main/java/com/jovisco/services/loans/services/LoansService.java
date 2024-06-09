package com.jovisco.services.loans.services;

import com.jovisco.services.loans.dtos.CreateLoanDto;
import com.jovisco.services.loans.dtos.LoanDto;

public interface LoansService {

  /**
   * 
   * @param mobileNumber
   */
  void createLoan(CreateLoanDto createLoanDto);

  /**
   * 
   * @param mobileNumber
   * @return loan details
   */
  LoanDto fetchLoan(String mobileNumber);

  /**
   * 
   * @param loanDto
   * @return boolean indicating if update was successful
   */
  boolean updateLoan(LoanDto loanDto);

  /**
   * 
   * @param mobileNumber
   * @return boolean indicating if delete was successful
   */
  boolean deleteLoan(String mobileNumber);
}
