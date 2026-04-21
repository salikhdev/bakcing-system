package uz.salikhdev.bakcingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.salikhdev.bakcingsystem.entity.LoanApplication;
import uz.salikhdev.bakcingsystem.entity.LoanApplicationStatus;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<LoanApplication> findByStatusOrderByCreatedAtDesc(LoanApplicationStatus status);
}
