package uz.salikhdev.bakcingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.salikhdev.bakcingsystem.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(Long fromId, Long toId);
}
