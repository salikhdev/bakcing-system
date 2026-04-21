package uz.salikhdev.bakcingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.salikhdev.bakcingsystem.entity.LoanPayment;
import uz.salikhdev.bakcingsystem.entity.LoanPaymentStatus;

import java.time.LocalDate;
import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanIdOrderByPaymentNumberAsc(Long loanId);

    List<LoanPayment> findByStatusInAndDueDateBefore(List<LoanPaymentStatus> statuses, LocalDate date);
}
