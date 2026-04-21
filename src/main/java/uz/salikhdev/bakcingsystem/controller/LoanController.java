package uz.salikhdev.bakcingsystem.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.salikhdev.bakcingsystem.dto.*;
import uz.salikhdev.bakcingsystem.service.LoanService;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loan")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/calculate")
    public ResponseEntity<LoanCalculateResponse> calculate(@RequestBody LoanCalculateRequest request) {
        return ResponseEntity.ok(loanService.calculate(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getByUser(userId));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getById(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getById(loanId));
    }

    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<List<LoanPaymentResponse>> getSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getSchedule(loanId));
    }

    @PostMapping("/{loanId}/pay")
    public ResponseEntity<LoanResponse> pay(
            @PathVariable Long loanId,
            @RequestBody LoanPayRequest request) {
        return ResponseEntity.ok(loanService.pay(loanId, request));
    }
}
