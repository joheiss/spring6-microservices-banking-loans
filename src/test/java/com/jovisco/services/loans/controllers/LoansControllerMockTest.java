package com.jovisco.services.loans.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jovisco.services.loans.constants.LoansConstants;
import com.jovisco.services.loans.dtos.LoanDto;
import com.jovisco.services.loans.dtos.CreateLoanDto;
import com.jovisco.services.loans.services.LoansService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoansController.class)
public class LoansControllerMockTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  LoansService loansService;

  final String mobileNumber = "+122234567890";

  @Test
  void testCreateLoan() throws Exception {

    var createDto = new CreateLoanDto(mobileNumber);

    mockMvc.perform(
        post("/api/v1/" + LoansController.LOANS_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"));

    // verify that the loans service's createLoan method was invoked
    verify(loansService, times(1)).createLoan(createDto);

  }

  @Test
  void testDeleteLoan() throws Exception {

    given(loansService.deleteLoan(any())).willReturn(true);

    mockMvc.perform(
        delete("/api/v1/" + LoansController.LOANS_MOBILENUMBER_PATH, mobileNumber))
        .andExpect(status().isOk());

    // verify that the loans service's deleteLoan method was invoked
    verify(loansService, times(1)).deleteLoan(mobileNumber);
  }

  @Test
  void testFetchLoan() throws Exception {

    given(loansService.fetchLoan(any())).willReturn(buildLoanDto(mobileNumber));

    mockMvc.perform(
        get("/api/v1/" + LoansController.LOANS_MOBILENUMBER_PATH, mobileNumber)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // verify that the loans service's deleteLoan method was invoked
    verify(loansService, times(1)).fetchLoan(mobileNumber);
  }

  @Test
  void testUpdateLoan() throws Exception {

    given(loansService.updateLoan(any())).willReturn(true);

    var updateDto = buildLoanDto(mobileNumber);
    mockMvc.perform(
        put("/api/v1/" + LoansController.LOANS_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // verify that the loans service's updateLoan method was invoked
    verify(loansService, times(1)).updateLoan(updateDto);
  }

  private LoanDto buildLoanDto(String mobileNumber) {

    return LoanDto.builder()
        .mobileNumber(mobileNumber)
        .loanNumber("123456789012")
        .loanType(LoansConstants.HOME_LOAN)
        .totalLoan(LoansConstants.NEW_LOAN_LIMIT)
        .amountPaid(0)
        .outstandingAmount(LoansConstants.NEW_LOAN_LIMIT)
        .build();
  }
}
