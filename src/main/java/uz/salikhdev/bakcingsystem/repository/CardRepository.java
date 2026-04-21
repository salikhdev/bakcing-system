package uz.salikhdev.bakcingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.salikhdev.bakcingsystem.entity.Card;
import uz.salikhdev.bakcingsystem.entity.CardStatus;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByAccountId(Long accountId);

    boolean existsByCardNumber(String cardNumber);

    List<Card> findByStatusNot(CardStatus status);

    Optional<Card> findByCardNumber(String cardNumber);
}