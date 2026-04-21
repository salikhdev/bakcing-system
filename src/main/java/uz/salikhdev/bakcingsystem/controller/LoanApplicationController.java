package uz.salikhdev.bakcingsystem.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.salikhdev.bakcingsystem.dto.LoanApplicationRejectRequest;
import uz.salikhdev.bakcingsystem.dto.LoanApplicationRequest;
import uz.salikhdev.bakcingsystem.dto.LoanApplicationResponse;
import uz.salikhdev.bakcingsystem.service.LoanApplicationService;

import java.util.List;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
@Tag(name = "Loan Application")
public class LoanApplicationController {

    private final LoanApplicationService applicationService;

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> apply(@RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(applicationService.apply(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanApplicationResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(applicationService.getByUser(userId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanApplicationResponse>> getPending() {
        return ResponseEntity.ok(applicationService.getPending());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponse> reject(
            @PathVariable Long id,
            @RequestBody LoanApplicationRejectRequest request) {
        return ResponseEntity.ok(applicationService.reject(id, request));
    }
}
