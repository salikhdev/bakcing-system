package uz.salikhdev.bakcingsystem.dto;

import lombok.Data;
import uz.salikhdev.bakcingsystem.entity.CardType;

@Data
public class CardCreateRequest {
    private CardType type;
}