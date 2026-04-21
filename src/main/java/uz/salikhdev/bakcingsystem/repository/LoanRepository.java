package uz.salikhdev.bakcingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.salikhdev.bakcingsystem.entity.LoanStatus;
import uz.salikhdev.bakcingsystem.entity.Loans;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loans, Long> {

    List<Loans> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Loans> findByStatus(LoanStatus status);
}
