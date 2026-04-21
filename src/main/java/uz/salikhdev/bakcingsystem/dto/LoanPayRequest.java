package uz.salikhdev.bakcingsystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanPayRequest {
    private Long fromAccountId;
    private BigDecimal amount;
}
