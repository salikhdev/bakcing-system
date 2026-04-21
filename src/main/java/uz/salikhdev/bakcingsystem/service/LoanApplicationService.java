package uz.salikhdev.bakcingsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.salikhdev.bakcingsystem.dto.*;
import uz.salikhdev.bakcingsystem.entity.*;
import uz.salikhdev.bakcingsystem.exception.BadRequestException;
import uz.salikhdev.bakcingsystem.exception.NotFoundException;
import uz.salikhdev.bakcingsystem.repository.AccountRepository;
import uz.salikhdev.bakcingsystem.repository.LoanApplicationRepository;
import uz.salikhdev.bakcingsystem.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final LoanCalculator loanCalculator;
    private final LoanDisbursementService loanDisbursementService;

    @Transactional
    public LoanApplicationResponse apply(LoanApplicationRequest request) {
        validateRequest(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new NotFoundException("Hisob topilmadi"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Hisob aktiv emas");
        }

        if (!account.getOwnerId().equals(user.getId()) || account.getOwnerType() != OwnerType.USER) {
            throw new BadRequestException("Hisob foydalanuvchiga tegishli emas");
        }

        BigDecimal rate = loanCalculator.getInterestRate(request.getLoanType());
        BigDecimal monthlyPayment = loanCalculator.calculateMonthlyPayment(
                request.getAmount(), rate, request.getDurationMonth());

        if (request.getMonthlyIncome() != null
                && monthlyPayment.compareTo(request.getMonthlyIncome().multiply(new BigDecimal("0.5"))) > 0) {
            throw new BadRequestException("Oylik to'lov daromadning 50% dan oshmasligi kerak");
        }

        LoanApplication application = LoanApplication.builder()
                .user(user)
                .disbursementAccount(account)
                .amount(request.getAmount())
                .durationMonth(request.getDurationMonth())
                .loanType(request.getLoanType())
                .monthlyIncome(request.getMonthlyIncome())
                .status(LoanApplicationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        applicationRepository.save(application);
        return toResponse(application);
    }

    @Transactional
    public LoanApplicationResponse approve(Long applicationId) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Ariza topilmadi"));

        if (application.getStatus() != LoanApplicationStatus.PENDING) {
            throw new BadRequestException("Faqat PENDING statusdagi arizani tasdiqlash mumkin");
        }

        application.setStatus(LoanApplicationStatus.APPROVED);
        application.setProcessedAt(LocalDateTime.now());
        applicationRepository.save(application);

        loanDisbursementService.disburse(application);

        return toResponse(application);
    }

    @Transactional
    public LoanApplicationResponse reject(Long applicationId, LoanApplicationRejectRequest request) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Ariza topilmadi"));

        if (application.getStatus() != LoanApplicationStatus.PENDING) {
            throw new BadRequestException("Faqat PENDING statusdagi arizani rad etish mumkin");
        }

        application.setStatus(LoanApplicationStatus.REJECTED);
        application.setRejectionReason(request.getReason());
        application.setProcessedAt(LocalDateTime.now());
        applicationRepository.save(application);

        return toResponse(application);
    }

    public List<LoanApplicationResponse> getByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Foydalanuvchi topilmadi");
        }
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    public List<LoanApplicationResponse> getPending() {
        return applicationRepository.findByStatusOrderByCreatedAtDesc(LoanApplicationStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    private void validateRequest(LoanApplicationRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Summa 0 dan katta bo'lishi kerak");
        }
        if (request.getDurationMonth() == null || request.getDurationMonth() < 1 || request.getDurationMonth() > 360) {
            throw new BadRequestException("Muddat 1 oydan 360 oygacha bo'lishi kerak");
        }
        if (request.getLoanType() == null) {
            throw new BadRequestException("Kredit turi ko'rsatilishi shart");
        }
    }

    private LoanApplicationResponse toResponse(LoanApplication a) {
        return LoanApplicationResponse.builder()
                .id(a.getId())
                .userId(a.getUser().getId())
                .accountId(a.getDisbursementAccount() != null ? a.getDisbursementAccount().getId() : null)
                .amount(a.getAmount())
                .durationMonth(a.getDurationMonth())
                .loanType(a.getLoanType().name())
                .monthlyIncome(a.getMonthlyIncome())
                .status(a.getStatus().name())
                .rejectionReason(a.getRejectionReason())
                .createdAt(a.getCreatedAt())
                .processedAt(a.getProcessedAt())
                .build();
    }
}
