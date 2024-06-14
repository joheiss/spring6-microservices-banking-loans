package com.jovisco.services.loans.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "Create Loan", description = "Schema to create a loan")
@NoArgsConstructor @AllArgsConstructor @Data
public class CreateLoanDto {

  @Schema(description = "Mobile phone number of customer", example = "+1 222 34567890")
  @NotEmpty(message = "Mobile number must not be empty")
  @Pattern(regexp = "^\\+[1-9]{1}[0-9]{3,14}$", message = "Mobile number must be valid")
  private String mobileNumber;
}
