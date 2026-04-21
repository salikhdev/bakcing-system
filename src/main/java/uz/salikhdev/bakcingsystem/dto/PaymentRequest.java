package uz.salikhdev.bakcingsystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
}
