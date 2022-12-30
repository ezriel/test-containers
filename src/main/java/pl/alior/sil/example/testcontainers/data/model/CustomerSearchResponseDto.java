package pl.alior.sil.example.testcontainers.data.model;

import lombok.Data;

import java.util.List;

@Data
public class CustomerSearchResponseDto {
    private List<CustomerResponseDto> customers;
}
