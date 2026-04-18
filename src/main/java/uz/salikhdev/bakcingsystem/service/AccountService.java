package uz.salikhdev.bakcingsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.salikhdev.bakcingsystem.dto.AccountCreateRequest;
import uz.salikhdev.bakcingsystem.dto.AccountResponse;
import uz.salikhdev.bakcingsystem.dto.AccountUpdateRequest;
import uz.salikhdev.bakcingsystem.entity.Account;
import uz.salikhdev.bakcingsystem.entity.AccountStatus;
import uz.salikhdev.bakcingsystem.entity.OwnerType;
import uz.salikhdev.bakcingsystem.exception.BadRequestException;
import uz.salikhdev.bakcingsystem.exception.NotFoundException;
import uz.salikhdev.bakcingsystem.repository.AccountRepository;
import uz.salikhdev.bakcingsystem.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountResponse createAccount(Long userId, AccountCreateRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Foydalanuvchi topilmadi");
        }

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "UZS")
                .status(AccountStatus.ACTIVE)
                .ownerType(OwnerType.USER)
                .ownerId(userId)
                .build();

        accountRepository.save(account);

        return toResponse(account);
    }

    public List<AccountResponse> getAccountsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Foydalanuvchi topilmadi");
        }

        return accountRepository.findByOwnerIdAndOwnerType(userId, OwnerType.USER)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Hisob topilmadi"));

        return toResponse(account);
    }

    public AccountResponse updateAccount(Long accountId, AccountUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Hisob topilmadi"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BadRequestException("Yopilgan hisobni tahrirlash mumkin emas");
        }

        if (request.getCurrency() != null) {
            account.setCurrency(request.getCurrency());
        }

        accountRepository.save(account);
        return toResponse(account);
    }

    public AccountResponse freezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Hisob topilmadi"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BadRequestException("Yopilgan hisobni muzlatish mumkin emas");
        }

        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
        return toResponse(account);
    }

    public AccountResponse unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Hisob topilmadi"));

        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new BadRequestException("Faqat muzlatilgan hisobni aktivlashtirish mumkin");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        return toResponse(account);
    }

    public void deleteAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Hisob topilmadi"));

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Balansda pul bor, hisobni yopish mumkin emas");
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus().name())
                .ownerId(account.getOwnerId())
                .build();
    }

    private String generateAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            StringBuilder sb = new StringBuilder("20208000900");
            for (int i = 0; i < 9; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}