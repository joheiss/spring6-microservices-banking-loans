package com.jovisco.services.loans.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Schema(name = "Loans", description = "Schema to transfer loan data")
@Data
@Builder
public class LoanDto {

  @Schema(description = "Mobile phone number of customer", example = "+1 222 34567890")
  @NotEmpty(message = "Mobile number must not be empty")
  @Pattern(regexp = "^\\+[1-9]{1}[0-9]{3,14}$", message = "Mobile number must be valid")
  private String mobileNumber;

  @Schema(description = "Loan number of the customer", example = "123456789012")
  @NotEmpty(message = "Loan number must not be empty")
  @Pattern(regexp = "(^$|[0-9]{12})", message = "Loan number must be 12 digits")
  private String loanNumber;

  @Schema(description = "Type of loan", example = "Home Loan")
  @NotEmpty(message = "Loan type must not be empty")
  private String loanType;

  @Schema(description = "Total loan amount", example = "123456")
  @Positive(message = "Total loan must be greater than zero")
  private int totalLoan;

  @Schema(description = "Total loan amount paid", example = "12345")
  @PositiveOrZero(message = "Amount paid must be greater than or equal zero")
  private int amountPaid;

  @Schema(description = "Total loan amount outstanding", example = "98765")
  @PositiveOrZero(message = "Outstanding amount must be greater than or equal zero")
  private int outstandingAmount;

}
