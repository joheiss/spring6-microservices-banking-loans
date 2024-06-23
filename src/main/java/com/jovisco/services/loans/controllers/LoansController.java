package com.jovisco.services.loans.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jovisco.services.loans.constants.LoansConstants;
import com.jovisco.services.loans.dtos.CreateLoanDto;
import com.jovisco.services.loans.dtos.ErrorResponseDto;
import com.jovisco.services.loans.dtos.LoanDto;
import com.jovisco.services.loans.dtos.ContactInfoDto;
import com.jovisco.services.loans.dtos.ResponseDto;
import com.jovisco.services.loans.services.LoansService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "CRUD REST APIs for Loans in Banking Microservices", description = "CRUD REST APIs to CREATE, READ, UPDATE and DELETE Loans in Banking Microservices")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class LoansController {

        public static final String LOANS_PATH = "/loans";
        public static final String LOANS_MOBILENUMBER_PATH = LOANS_PATH + "/{mobileNumber}";
        public static final String LOANS_VERSION_PATH = LOANS_PATH + "/version";

        private final LoansService loansService;

        private final ContactInfoDto loansContactInfoDto;

        private final Environment environment;

        @Value("${build.version}")
        private String buildVersion;

        @Operation(summary = "Fetch a single loan by the customer's mobile number", description = "Fetch data from loan for a given mobile number")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
                        @ApiResponse(responseCode = "404", description = "HTTP Status NOT_FOUND", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/loans/+122234567890\", \"errorCode\": \"404\", \"errorMessage\": \"Loan not found ...\", \"errorTime\": \"2024-07-04T11:12:13\"}")
                        }, mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "500", description = "HTTP Status INTERNAL_SERVER_ERROR", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/loans\", \"errorCode\": \"500\", \"errorMessage\": \"An error occurred ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE))
        })

        @GetMapping(LOANS_MOBILENUMBER_PATH)
        public ResponseEntity<LoanDto> fetchLoan(
                        @RequestHeader("jovisco-banking-correlation-id") String correlationId,
                        @PathVariable String mobileNumber) {

                log.debug("fetchLoan started");

                var loanDto = loansService.fetchLoan(mobileNumber);

                log.debug("fetchLoan finished");

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(loanDto);
        }

        @Operation(summary = "Create a loan", description = "Create a loan")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "HTTP Status CREATED", content = @Content(schema = @Schema(implementation = ResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"statusCode\": \"201\", \"statusMessage\": \"Loan created successfully\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "400", description = "HTTP Status BAD_REQUEST", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "500", description = "HTTP Status INTERNAL_SERVER_ERROR", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/accounts\", \"errorCode\": \"500\", \"errorMessage\": \"An error occurred ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE))
        })

        @PostMapping(LOANS_PATH)
        public ResponseEntity<ResponseDto> createLoan(@Valid @RequestBody CreateLoanDto createLoanDto) {

                // create loan
                loansService.createLoan(createLoanDto);

                // store mobile number in location header
                var headers = new HttpHeaders();
                headers.add("Location", LOANS_PATH + "/" + createLoanDto.getMobileNumber());

                var body = ResponseDto.builder()
                                .statusCode(LoansConstants.STATUS_201)
                                .statusMessage(LoansConstants.MESSAGE_201)
                                .build();

                return new ResponseEntity<ResponseDto>(body, headers, HttpStatus.CREATED);
        }

        @Operation(summary = "Update a loan", description = "Update a loan")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
                        @ApiResponse(responseCode = "400", description = "HTTP Status BAD_REQUEST", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "HTTP Status NOT_FOUND", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/loans/+122234567890\", \"errorCode\": \"404\", \"errorMessage\": \"Loan not found ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "500", description = "HTTP Status INTERNAL_SERVER_ERROR", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/loans\", \"errorCode\": \"500\", \"errorMessage\": \"An error occurred ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE))
        })

        @PutMapping(LOANS_PATH)
        public ResponseEntity<ResponseDto> updateLoan(@Valid @RequestBody LoanDto loanDto) {

                var isUpdated = loansService.updateLoan(loanDto);

                if (isUpdated) {
                        return ResponseEntity
                                        .status(HttpStatus.OK)
                                        .body(ResponseDto.builder()
                                                        .statusCode(LoansConstants.STATUS_200)
                                                        .statusMessage(LoansConstants.MESSAGE_200)
                                                        .build());
                } else {
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ResponseDto.builder()
                                                        .statusCode(LoansConstants.STATUS_500)
                                                        .statusMessage(LoansConstants.MESSAGE_500)
                                                        .build());

                }
        }

        @Operation(summary = "Delete a loan", description = "Delete aloan by mobile number")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
                        @ApiResponse(responseCode = "404", description = "HTTP Status NOT_FOUND", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/loans/+122234567890\", \"errorCode\": \"404\", \"errorMessage\": \"Loan not found ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "500", description = "HTTP Status INTERNAL_SERVER_ERROR", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/loans\", \"errorCode\": \"500\", \"errorMessage\": \"An error occurred ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @DeleteMapping(LOANS_MOBILENUMBER_PATH)
        public ResponseEntity<ResponseDto> deleteLoan(@PathVariable String mobileNumber) {

                var isDeleted = loansService.deleteLoan(mobileNumber);

                if (isDeleted) {
                        return ResponseEntity
                                        .status(HttpStatus.OK)
                                        .body(ResponseDto.builder()
                                                        .statusCode(LoansConstants.STATUS_200)
                                                        .statusMessage(LoansConstants.MESSAGE_200)
                                                        .build());
                } else {
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ResponseDto.builder()
                                                        .statusCode(LoansConstants.STATUS_500)
                                                        .statusMessage(LoansConstants.MESSAGE_500)
                                                        .build());
                }
        }

        @Operation(summary = "Get build information", description = "Get the current build version that is deployed for this service")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "HTTP Status OK", content = @Content(examples = {
                                        @ExampleObject(value = "1.0.0") }, mediaType = MediaType.TEXT_PLAIN_VALUE)),
                        @ApiResponse(responseCode = "500", description = "HTTP Status INTERNAL_SERVER_ERROR", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/cards/version\", \"errorCode\": \"500\", \"errorMessage\": \"An error occurred ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE))
        })

        @GetMapping(path = LOANS_VERSION_PATH, produces = MediaType.TEXT_PLAIN_VALUE)
        public ResponseEntity<String> getBuildVersion() {
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(buildVersion);
        }

        @Operation(summary = "Get Java version", description = "Get the current Java version that is deployed for this service")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "HTTP Status OK", content = @Content(examples = {
                                        @ExampleObject(value = "1.0.0") }, mediaType = MediaType.TEXT_PLAIN_VALUE)),
                        @ApiResponse(responseCode = "500", description = "HTTP Status INTERNAL_SERVER_ERROR", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/cards/java-version\", \"errorCode\": \"500\", \"errorMessage\": \"An error occurred ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @RateLimiter(name = "getJavaVersion", fallbackMethod = "getJavaVersionFallback")

        @GetMapping(path = LOANS_PATH + "/java-version", produces = MediaType.TEXT_PLAIN_VALUE)
        public ResponseEntity<String> getJavaVersion() {
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(environment.getProperty("JAVA_HOME"));
        }

        public ResponseEntity<String> getJavaVersionFallback(Throwable throwable) {
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body("Java 21");
        }

        @Operation(summary = "Get environment variable", description = "Get the current value of an environment variable that is deployed for this service")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "HTTP Status OK", content = @Content(examples = {
                                        @ExampleObject(value = "anything") }, mediaType = MediaType.TEXT_PLAIN_VALUE)),
                        @ApiResponse(responseCode = "500", description = "HTTP Status INTERNAL_SERVER_ERROR", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/cards/java-version\", \"errorCode\": \"500\", \"errorMessage\": \"An error occurred ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE))
        })

        @GetMapping(path = LOANS_PATH + "/env-variable/{envVariable}", produces = MediaType.TEXT_PLAIN_VALUE)
        public ResponseEntity<String> getEnvVariable(@PathVariable String envVariable) {
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(environment.getProperty(envVariable));
        }

        @Operation(summary = "Get contact information", description = "Get contact information for this service")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "HTTP Status OK", content = @Content(schema = @Schema(implementation = ContactInfoDto.class), examples = {
                                        @ExampleObject(value = "{\"message\": \"Welcome to ...\", \"contact\": {\"name\": \"Jane Doe\", \"email\": \"jane@example.com\"}, \"support\": [\"+1 222 333 4444\", \"+1 555 666 7777\"]}") }, mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "500", description = "HTTP Status INTERNAL_SERVER_ERROR", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class), examples = {
                                        @ExampleObject(value = "{\"apiPath\": \"uri=/api/v1/cards/contact-info\", \"errorCode\": \"500\", \"errorMessage\": \"An error occurred ...\", \"errorTime\": \"2024-07-04T11:12:13\"}") }, mediaType = MediaType.APPLICATION_JSON_VALUE))
        })

        @GetMapping(path = LOANS_PATH + "/contact-info", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ContactInfoDto> getContactInfo() {
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(loansContactInfoDto);
        }

}
