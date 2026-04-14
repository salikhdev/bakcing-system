package uz.salikhdev.bakcingsystem.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String phone;
    private String password;
    private String passport;
    private String pinfl;
}
