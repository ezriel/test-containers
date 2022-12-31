package pl.alior.sil.example.testcontainers.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerSearchResponseDto {
    private List<CustomerResponseDto> customers;
}
