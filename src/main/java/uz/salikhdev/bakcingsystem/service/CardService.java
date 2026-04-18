package uz.salikhdev.bakcingsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.salikhdev.bakcingsystem.dto.CardCreateRequest;
import uz.salikhdev.bakcingsystem.dto.CardResponse;
import uz.salikhdev.bakcingsystem.entity.*;
import uz.salikhdev.bakcingsystem.exception.BadRequestException;
import uz.salikhdev.bakcingsystem.exception.NotFoundException;
import uz.salikhdev.bakcingsystem.repository.AccountRepository;
import uz.salikhdev.bakcingsystem.repository.CardRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public CardResponse createCard(Long accountId, CardCreateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Hisob topilmadi"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Faqat aktiv hisobga karta ochish mumkin");
        }

        String cardNumber = generateCardNumber(request.getType());
        String expiry = LocalDate.now().plusYears(3)
                .format(DateTimeFormatter.ofPattern("MM/yy"));

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .expiry(expiry)
                .account(account)
                .type(request.getType())
                .status(CardStatus.ACTIVE)
                .build();

        cardRepository.save(card);
        return toResponse(card);
    }

    public List<CardResponse> getCardsByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Hisob topilmadi");
        }

        return cardRepository.findByAccountId(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CardResponse getCardById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Karta topilmadi"));
        return toResponse(card);
    }

    public CardResponse blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Karta topilmadi"));

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new BadRequestException("Muddati tugagan kartani bloklash mumkin emas");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        return toResponse(card);
    }

    public CardResponse unblockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Karta topilmadi"));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new BadRequestException("Faqat bloklangan kartani aktivlashtirish mumkin");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        return toResponse(card);
    }

    private CardResponse toResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .expiry(card.getExpiry())
                .type(card.getType().name())
                .status(card.getStatus().name())
                .accountId(card.getAccount().getId())
                .build();
    }

    private String generateCardNumber(CardType type) {
        String prefix = switch (type) {
            case UZCARD -> "8600";
            case HUMO -> "9860";
            case VISA -> "4278";
            case MASTERCARD -> "5425";
        };

        Random random = new Random();
        String cardNumber;
        do {
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < 12; i++) {
                sb.append(random.nextInt(10));
            }
            cardNumber = sb.toString();
        } while (cardRepository.existsByCardNumber(cardNumber));

        return cardNumber;
    }
}
