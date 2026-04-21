package uz.salikhdev.bakcingsystem.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.salikhdev.bakcingsystem.dto.*;
import uz.salikhdev.bakcingsystem.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/card-transfer")
    public ResponseEntity<TransactionResponse> cardTransfer(@RequestBody CardTransferRequest request) {
        return ResponseEntity.ok(transactionService.cardTransfer(request));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody DepositRequest request) {
        return ResponseEntity.ok(transactionService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(transactionService.withdraw(request));
    }

    @PostMapping("/payment")
    public ResponseEntity<TransactionResponse> payment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(transactionService.payment(request));
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionResponse>> history(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getAccountHistory(accountId));
    }
}
