package uz.salikhdev.bakcingsystem.dto;

import lombok.Data;
import uz.salikhdev.bakcingsystem.entity.LoanType;

import java.math.BigDecimal;

@Data
public class LoanCalculateRequest {
    private BigDecimal amount;
    private Integer durationMonth;
    private LoanType loanType;
}
