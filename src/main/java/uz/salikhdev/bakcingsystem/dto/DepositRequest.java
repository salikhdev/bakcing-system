package uz.salikhdev.bakcingsystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {
    private Long accountId;
    private BigDecimal amount;
}
