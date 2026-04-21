package uz.salikhdev.bakcingsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.salikhdev.bakcingsystem.dto.*;
import uz.salikhdev.bakcingsystem.entity.*;
import uz.salikhdev.bakcingsystem.exception.BadRequestException;
import uz.salikhdev.bakcingsystem.exception.NotFoundException;
import uz.salikhdev.bakcingsystem.repository.AccountRepository;
import uz.salikhdev.bakcingsystem.repository.LoanPaymentRepository;
import uz.salikhdev.bakcingsystem.repository.LoanRepository;
import uz.salikhdev.bakcingsystem.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanCalculator loanCalculator;

    public LoanCalculateResponse calculate(LoanCalculateRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Summa 0 dan katta bo'lishi kerak");
        }
        if (request.getDurationMonth() == null || request.getDurationMonth() < 1) {
            throw new BadRequestException("Muddat noto'g'ri");
        }

        BigDecimal rate = loanCalculator.getInterestRate(request.getLoanType());
        BigDecimal monthlyPayment = loanCalculator.calculateMonthlyPayment(
                request.getAmount(), rate, request.getDurationMonth());
        BigDecimal totalAmount = loanCalculator.calculateTotalAmount(monthlyPayment, request.getDurationMonth());
        BigDecimal totalInterest = totalAmount.subtract(request.getAmount());

        return LoanCalculateResponse.builder()
                .amount(request.getAmount())
                .durationMonth(request.getDurationMonth())
                .interestRate(rate)
                .monthlyPayment(monthlyPayment)
                .totalAmount(totalAmount)
                .totalInterest(totalInterest)
                .build();
    }

    public List<LoanResponse> getByUser(Long userId) {
        return loanRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toLoanResponse).toList();
    }

    public LoanResponse getById(Long loanId) {
        Loans loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Kredit topilmadi"));
        return toLoanResponse(loan);
    }

    public List<LoanPaymentResponse> getSchedule(Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new NotFoundException("Kredit topilmadi");
        }
        return loanPaymentRepository.findByLoanIdOrderByPaymentNumberAsc(loanId)
                .stream().map(this::toPaymentResponse).toList();
    }

    @Transactional
    public LoanResponse pay(Long loanId, LoanPayRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Summa 0 dan katta bo'lishi kerak");
        }

        Loans loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Kredit topilmadi"));

        if (loan.getStatus() == LoanStatus.CLOSED) {
            throw new BadRequestException("Kredit yopilgan");
        }

        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new NotFoundException("Hisob topilmadi"));

        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Hisob aktiv emas");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Hisobda yetarli mablag' yo'q");
        }

        List<LoanPayment> unpaidPayments = loanPaymentRepository
                .findByLoanIdOrderByPaymentNumberAsc(loanId)
                .stream()
                .filter(p -> p.getStatus() != LoanPaymentStatus.PAID)
                .toList();

        if (unpaidPayments.isEmpty()) {
            throw new BadRequestException("To'lanishi kerak bo'lgan summa yo'q");
        }

        BigDecimal remaining = request.getAmount();
        for (LoanPayment payment : unpaidPayments) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal needed = payment.getTotalAmount().add(payment.getPenalty()).subtract(payment.getPaidAmount());
            BigDecimal toPay = remaining.min(needed);

            payment.setPaidAmount(payment.getPaidAmount().add(toPay));
            remaining = remaining.subtract(toPay);

            if (payment.getPaidAmount().compareTo(payment.getTotalAmount().add(payment.getPenalty())) >= 0) {
                payment.setStatus(LoanPaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
            } else {
                payment.setStatus(LoanPaymentStatus.PARTIAL);
            }

            loanPaymentRepository.save(payment);
        }

        BigDecimal actuallyPaid = request.getAmount().subtract(remaining);
        fromAccount.setBalance(fromAccount.getBalance().subtract(actuallyPaid));
        accountRepository.save(fromAccount);

        loan.setRemainingAmount(loan.getRemainingAmount().subtract(actuallyPaid));
        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setRemainingAmount(BigDecimal.ZERO);
            loan.setNextPaymentDate(null);
        } else {
            LoanPayment nextUnpaid = loanPaymentRepository
                    .findByLoanIdOrderByPaymentNumberAsc(loanId)
                    .stream()
                    .filter(p -> p.getStatus() != LoanPaymentStatus.PAID)
                    .findFirst()
                    .orElse(null);
            if (nextUnpaid != null) {
                loan.setNextPaymentDate(nextUnpaid.getDueDate());
            }
        }
        loanRepository.save(loan);

        Transaction tx = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(null)
                .amount(actuallyPaid)
                .fee(BigDecimal.ZERO)
                .type(TransactionType.PAYMENT)
                .status(TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        return toLoanResponse(loan);
    }

    private LoanResponse toLoanResponse(Loans l) {
        return LoanResponse.builder()
                .id(l.getId())
                .userId(l.getUser().getId())
                .accountId(l.getAccount() != null ? l.getAccount().getId() : null)
                .loanType(l.getLoanType() != null ? l.getLoanType().name() : null)
                .amount(l.getAmount())
                .interestRate(l.getInterestRate())
                .durationMonth(l.getDurationMonth())
                .monthlyPayment(l.getMonthlyPayment())
                .totalAmount(l.getTotalAmount())
                .remainingAmount(l.getRemainingAmount())
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .nextPaymentDate(l.getNextPaymentDate())
                .overdueDays(l.getOverdueDays())
                .status(l.getStatus().name())
                .build();
    }

    private LoanPaymentResponse toPaymentResponse(LoanPayment p) {
        return LoanPaymentResponse.builder()
                .id(p.getId())
                .paymentNumber(p.getPaymentNumber())
                .dueDate(p.getDueDate())
                .paidAt(p.getPaidAt())
                .principal(p.getPrincipal())
                .interest(p.getInterest())
                .penalty(p.getPenalty())
                .totalAmount(p.getTotalAmount())
                .paidAmount(p.getPaidAmount())
                .status(p.getStatus().name())
                .build();
    }
}
