package pl.alior.sil.example.testcontainers.data.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomerResponseDto extends CustomerDto {
    private Long customerId;
    private LocalDateTime createdAt;
}
