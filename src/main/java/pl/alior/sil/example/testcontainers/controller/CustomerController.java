package pl.alior.sil.example.testcontainers.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.alior.sil.example.testcontainers.data.model.CustomerDto;
import pl.alior.sil.example.testcontainers.data.model.CustomerResponseDto;
import pl.alior.sil.example.testcontainers.data.model.CustomerSearchResponseDto;
import pl.alior.sil.example.testcontainers.service.CustomerService;

import javax.validation.Valid;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping(value = "/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CustomerResponseDto getCustomer(@PathVariable Long customerId) {
        return customerService.getCustomer(customerId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CustomerResponseDto createCustomer(@Valid @RequestBody CustomerDto customer) {
        return customerService.createCustomer(customer);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CustomerSearchResponseDto searchCustomer(@RequestParam(value = "pesel", required = false) String pesel, @RequestParam(value = "firstName", required = false) String firstName) {
        return null;
    }
}
