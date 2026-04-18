package uz.salikhdev.bakcingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.salikhdev.bakcingsystem.entity.Card;
import uz.salikhdev.bakcingsystem.entity.CardStatus;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByAccountId(Long accountId);

    boolean existsByCardNumber(String cardNumber);

    List<Card> findByStatusNot(CardStatus status);
}