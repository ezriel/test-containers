package pl.alior.sil.example.testcontainers.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.alior.sil.example.testcontainers.data.entity.mwapp.CustomerEntity;
import pl.alior.sil.example.testcontainers.data.model.CustomerDto;
import pl.alior.sil.example.testcontainers.data.model.CustomerResponseDto;
import pl.alior.sil.example.testcontainers.data.model.CustomerSearchResponseDto;
import pl.alior.sil.example.testcontainers.data.repository.mwapp.CustomerRepository;
import pl.alior.sil.example.testcontainers.ex.CustomerExistException;
import pl.alior.sil.example.testcontainers.ex.CustomerNotFoundException;

import java.util.List;

@Service
@Slf4j
public class CustomerService {

    private final CustomerRepository repository;
    private final ModelMapper modelMapper;

    public CustomerService(CustomerRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public CustomerResponseDto getCustomer(Long customerId) {
        CustomerEntity customerEntity = repository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
        return modelMapper.map(customerEntity, CustomerResponseDto.class);
    }

    public CustomerResponseDto createCustomer(CustomerDto dto) {
        int count = repository.countByPesel(dto.getPesel());
        if (count > 0) {
            throw new CustomerExistException();
        }
        CustomerEntity entity = modelMapper.map(dto, CustomerEntity.class);
        repository.save(entity);
        return modelMapper.map(entity, CustomerResponseDto.class);
    }

    public CustomerSearchResponseDto searchCustomer(String pesel, String firstName) {
        List<CustomerEntity> searchDbResult = repository.findByFirstNameOrPesel(firstName, pesel);
        log.info("Found {} customers", searchDbResult.size());
        return new CustomerSearchResponseDto(searchDbResult.stream().map(entity -> modelMapper.map(entity, CustomerResponseDto.class)).toList());
    }
}
