package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.CustomerNotFoundException;
import com.cosmocats.marketplace.application.service.CustomerService;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import com.cosmocats.marketplace.presentation.controller.CustomerController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private CustomerService customerService;

    private static CustomerEntity customer(UUID id, String email, String fullName) {
        CustomerEntity entity = new CustomerEntity(email, fullName);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    @Test
    void createShouldReturn201WithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.create("cat@cosmo.cats", "Space Cat"))
                .thenReturn(customer(id, "cat@cosmo.cats", "Space Cat"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cat@cosmo.cats\",\"fullName\":\"Space Cat\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/customers/" + id))
                .andExpect(jsonPath("$.email").value("cat@cosmo.cats"))
                .andExpect(jsonPath("$.fullName").value("Space Cat"));
    }

    @Test
    void createShouldReturn400ForInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"fullName\":\"Space Cat\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllShouldReturnCustomers() throws Exception {
        when(customerService.findAll()).thenReturn(List.of(
                customer(UUID.randomUUID(), "a@cosmo.cats", "A"),
                customer(UUID.randomUUID(), "b@cosmo.cats", "B")));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findByIdShouldReturnCustomerOr404() throws Exception {
        UUID id = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        when(customerService.findById(id)).thenReturn(customer(id, "a@cosmo.cats", "A"));
        when(customerService.findById(missing)).thenThrow(new CustomerNotFoundException(missing));

        mockMvc.perform(get("/api/customers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
        mockMvc.perform(get("/api/customers/" + missing))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/customers/" + id))
                .andExpect(status().isNoContent());

        verify(customerService).delete(id);
    }
}
