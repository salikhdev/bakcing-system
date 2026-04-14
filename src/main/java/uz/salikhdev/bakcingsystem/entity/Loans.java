package uz.salikhdev.bakcingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal amount;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "duration_month")
    private Integer durationMonth;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Column(name = "monthly_payment")
    private BigDecimal monthlyPayment;
}
