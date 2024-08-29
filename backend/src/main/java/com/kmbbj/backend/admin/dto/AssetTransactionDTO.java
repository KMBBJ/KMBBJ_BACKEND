package com.kmbbj.backend.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
public class AssetTransactionDTO {
    private Long id;
    private BigDecimal amount;
    private String transactionType;
    private Date transactionDate;


}
