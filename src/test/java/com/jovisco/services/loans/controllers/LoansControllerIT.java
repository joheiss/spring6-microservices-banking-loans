package com.jovisco.services.loans.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jovisco.services.loans.dtos.CreateLoanDto;
import com.jovisco.services.loans.exceptions.LoanAlreadyExistsException;
import com.jovisco.services.loans.exceptions.ResourceNotFoundException;
import com.jovisco.services.loans.repositories.LoansRepository;
import com.jovisco.services.loans.services.LoansService;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class LoansControllerIT {

  @Autowired
  LoansController loansController;

  @Autowired
  LoansService loansService;

  @Autowired
  LoansRepository loansRepository;

  @Autowired
  WebApplicationContext wac;

  @Autowired
  ObjectMapper objectMapper;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Transactional
  @Rollback
  @Test
  void testCreateAccountMvc() throws Exception {

    var createDto = new CreateLoanDto("+122234567890");

    mockMvc.perform(
        post("/api/v1/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isCreated())
        .andReturn();
  }

  @Transactional
  @Rollback
  @Test
  void testCreateLoan() {

    var createDto = new CreateLoanDto("+122234567890");
    var response = loansController.createLoan(createDto);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var location = response.getHeaders().getLocation();
    assertThat(location).isNotNull();

    // get id from location & verify that the loan is really present on the database
    if (location != null) {
      var segments = location.getPath().split("/");
      var mobileNumber = segments[segments.length - 1];
      assertThat(mobileNumber).isNotNull();
      var loan = loansRepository.findByMobileNumber(mobileNumber);
      assertTrue(loan.isPresent());
    }
  }

  @Transactional
  @Rollback
  @Test
  void testCreateLoanWithAlreadyExistsError() {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    loansController.createLoan(createDto);

    // ... then try to create the same loan again which should throw an exception
    assertThatExceptionOfType(LoanAlreadyExistsException.class)
        .isThrownBy(() -> loansController.createLoan(createDto));
  }

  @Transactional
  @Rollback
  @Test
  void testCreateLoanWithValidationError() {

    // mobile number is invalid
    var createDto = new CreateLoanDto("INVALID");

    // ... should throw an exception
    assertThatExceptionOfType(ConstraintViolationException.class)
        .isThrownBy(() -> loansController.createLoan(createDto))
        .withMessageContaining("mobile");
  }

  @Transactional
  @Rollback
  @Test
  void testDeleteLoanMvc() throws Exception {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    loansController.createLoan(createDto);

    mockMvc.perform(
        delete("/api/v1/loans/{mobileNumber}", createDto.getMobileNumber()))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Transactional
  @Rollback
  @Test
  void testDeleteLoan() {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    var response = loansController.createLoan(createDto);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var location = response.getHeaders().getLocation();

    // get id from location and delete account
    var segments = location.getPath().split("/");
    var mobileNumber = segments[segments.length - 1];

    response = loansController.deleteLoan(mobileNumber);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // verify that the loan has been deleted from database
    assertFalse(loansRepository.findByMobileNumber(mobileNumber).isPresent());
  }

  @Transactional
  @Rollback
  @Test
  void testFetchLoanMvc() throws Exception {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    loansController.createLoan(createDto);

    mockMvc.perform(
        get("/api/v1/loans/{mobileNumber}", createDto.getMobileNumber()))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Transactional
  @Rollback
  @Test
  void testFetchLoan() {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    loansController.createLoan(createDto);

    // check that loan can be fetched
    var response = loansController.fetchLoan(createDto.getMobileNumber());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getMobileNumber()).isEqualTo(createDto.getMobileNumber());
  }

  @Test
  void testFetchAccountWithNotFound() {

    // check that an exception is thrown
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> loansController.fetchLoan("+999999999999"));
  }

  @Transactional
  @Rollback
  @Test
  void testUpdateLoanMvc() throws Exception {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    loansController.createLoan(createDto);

    // ... fetch it, and update fields
    var loanDto = loansController.fetchLoan(createDto.getMobileNumber()).getBody();
    loanDto.setTotalLoan(33333);
    loanDto.setAmountPaid(22222);
    loanDto.setOutstandingAmount(11111);

    mockMvc.perform(
        put("/api/v1/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loanDto)))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Transactional
  @Rollback
  @Test
  void testUpdateLoan() {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    loansController.createLoan(createDto);

    // ... fetch it, and modify fields
    var loanDto = loansController.fetchLoan(createDto.getMobileNumber()).getBody();
    loanDto.setTotalLoan(33333);
    loanDto.setAmountPaid(22222);
    loanDto.setOutstandingAmount(11111);

    // finally update the loan with the modified values, and check that it worked
    var response = loansController.updateLoan(loanDto);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // check that fields have been updated with the correct values
    var updated = loansController.fetchLoan(loanDto.getMobileNumber()).getBody();
    assertThat(updated.getTotalLoan()).isEqualTo(loanDto.getTotalLoan());
    assertThat(updated.getAmountPaid()).isEqualTo(loanDto.getAmountPaid());
    assertThat(updated.getOutstandingAmount()).isEqualTo(loanDto.getOutstandingAmount());
  }

  @Transactional
  @Rollback
  @Test
  void testUpdateLoanWithValidationErrors() {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    loansController.createLoan(createDto);

    // ... fetch it, and modify fields
    var loanDto = loansController.fetchLoan(createDto.getMobileNumber()).getBody();
    loanDto.setTotalLoan(-33333);
    loanDto.setAmountPaid(-22222);
    loanDto.setOutstandingAmount(-11111);
    loanDto.setLoanType("");

    assertThatExceptionOfType(ConstraintViolationException.class)
        .isThrownBy(() -> loansController.updateLoan(loanDto))
        .withMessageContainingAll("total", "paid", "outstanding", "type");
  }

  @Transactional
  @Rollback
  @Test
  void testUpdateLoanWithMoreValidationErrors() {

    // first create a test loan
    var createDto = new CreateLoanDto("+122234567890");
    loansController.createLoan(createDto);

    // ... fetch it, and modify fields
    var loanDto = loansController.fetchLoan(createDto.getMobileNumber()).getBody();
    loanDto.setMobileNumber(null);
    loanDto.setTotalLoan(0);
    loanDto.setLoanType(null);

    assertThatExceptionOfType(ConstraintViolationException.class)
        .isThrownBy(() -> loansController.updateLoan(loanDto))
        .withMessageContainingAll("mobile", "total", "type");
  }
}
