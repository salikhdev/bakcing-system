package uz.salikhdev.bakcingsystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardTransferRequest {
    private String fromCardNumber;
    private String toCardNumber;
    private BigDecimal amount;
}
