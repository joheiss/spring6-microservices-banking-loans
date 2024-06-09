package com.jovisco.services.loans.services;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.jovisco.services.loans.constants.LoansConstants;
import com.jovisco.services.loans.dtos.CreateLoanDto;
import com.jovisco.services.loans.dtos.LoanDto;
import com.jovisco.services.loans.entities.Loan;
import com.jovisco.services.loans.exceptions.LoanAlreadyExistsException;
import com.jovisco.services.loans.exceptions.ResourceNotFoundException;
import com.jovisco.services.loans.mappers.LoanMapper;
import com.jovisco.services.loans.repositories.LoansRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LoansServiceImpl implements LoansService {

  private final LoansRepository loansRepository;

  @Override
  public void createLoan(CreateLoanDto createLoanDto) {

    // first check if there is already a loan for the given mobile number
    loansRepository
        .findByMobileNumber(createLoanDto.getMobileNumber())
        .ifPresent(c -> {
          throw new LoanAlreadyExistsException("Loan already exists for mobile number: " + createLoanDto
              .getMobileNumber());
        });

    // prepare data for new loan
    var loan = buildNewLoan(createLoanDto);
    loansRepository.save(loan);

  }

  private Loan buildNewLoan(CreateLoanDto createLoanDto) {

    long randomNumber = 100000000000L + new Random().nextInt(900000000);

    return Loan.builder()
        .mobileNumber(createLoanDto.getMobileNumber())
        .loanNumber(Long.toString(randomNumber))
        .loanType(LoansConstants.HOME_LOAN)
        .totalLoan(LoansConstants.NEW_LOAN_LIMIT)
        .amountPaid(0)
        .outstandingAmount(LoansConstants.NEW_LOAN_LIMIT)
        .build();
  }

  @Override
  public LoanDto fetchLoan(String mobileNumber) {
    var loan = loansRepository
        .findByMobileNumber(mobileNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "mobile number", mobileNumber));

    return LoanMapper.mapToLoanDto(loan);
  }

  @Override
  public boolean updateLoan(LoanDto loanDto) {

    // first check if loan exists
    var loan = loansRepository
        .findByLoanNumber(loanDto.getLoanNumber())
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "loan number", loanDto.getLoanNumber()));

    // update values as requested
    var updates = modifyLoan(loan, loanDto);
    loansRepository.save(updates);

    return true;
  }

  private Loan modifyLoan(Loan loan, LoanDto loanDto) {

    loan.setMobileNumber(loanDto.getMobileNumber());
    loan.setLoanType(loanDto.getLoanType());
    loan.setTotalLoan(loanDto.getTotalLoan());
    loan.setAmountPaid(loanDto.getAmountPaid());
    loan.setOutstandingAmount(loanDto.getOutstandingAmount());

    return loan;
  }

  @Override
  public boolean deleteLoan(String mobileNumber) {

    // first check if loan exists
    var loan = loansRepository
        .findByMobileNumber(mobileNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "mobile number", mobileNumber));

    // delete loan by id
    loansRepository.deleteById(loan.getId());

    return true;
  }
}
