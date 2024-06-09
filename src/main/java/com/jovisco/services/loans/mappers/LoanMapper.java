package com.jovisco.services.loans.mappers;

import com.jovisco.services.loans.dtos.LoanDto;
import com.jovisco.services.loans.entities.Loan;

public class LoanMapper {

  /**
   * 
   * @param loan
   * @return loan DTO
   */
  public static LoanDto mapToLoanDto(Loan loan) {

    return LoanDto.builder()
        .mobileNumber(loan.getMobileNumber())
        .loanNumber(loan.getLoanNumber())
        .loanType(loan.getLoanType())
        .totalLoan(loan.getTotalLoan())
        .amountPaid((loan.getAmountPaid()))
        .outstandingAmount(loan.getOutstandingAmount())
        .build();
  }

  public static Loan mapToLoan(LoanDto loanDto) {

    return Loan.builder()
        .mobileNumber(loanDto.getMobileNumber())
        .loanNumber(loanDto.getLoanNumber())
        .loanType(loanDto.getLoanType())
        .totalLoan(loanDto.getTotalLoan())
        .amountPaid((loanDto.getAmountPaid()))
        .outstandingAmount(loanDto.getOutstandingAmount())
        .build();
  }
}
