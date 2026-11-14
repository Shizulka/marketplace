package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.InsufficientStockException;
import com.cosmocats.marketplace.application.exception.OrderNotFoundException;
import com.cosmocats.marketplace.application.service.OrderDetails;
import com.cosmocats.marketplace.application.service.OrderFacade;
import com.cosmocats.marketplace.application.service.OrderService.OrderLine;
import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.presentation.controller.OrderController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private OrderFacade orderFacade;

    private static OrderDetails order(UUID id, UUID customerId, UUID productId, OrderStatus status) {
        return new OrderDetails(id, "CC-ABCDEF1234", customerId, status, Instant.parse("2026-01-01T10:00:00Z"),
                new BigDecimal("20.00"),
                List.of(new OrderDetails.Item(productId, "Yarn", 2, new BigDecimal("10.00"))));
    }

    @Test
    void createShouldReturn201WithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(orderFacade.create(customerId, List.of(new OrderLine(productId, 2))))
                .thenReturn(order(id, customerId, productId, OrderStatus.NEW));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"items\":"
                                + "[{\"productId\":\"" + productId + "\",\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/" + id))
                .andExpect(jsonPath("$.orderNumber").value("CC-ABCDEF1234"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items[0].productName").value("Yarn"))
                .andExpect(jsonPath("$.totalPrice").value(20.00));
    }

    @Test
    void createShouldReturn400ForEmptyItemsOrBadQuantity() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"items\":[]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"items\":"
                                + "[{\"productId\":\"" + UUID.randomUUID() + "\",\"quantity\":0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShouldReturn409WhenStockIsInsufficient() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(orderFacade.create(customerId, List.of(new OrderLine(productId, 5))))
                .thenThrow(new InsufficientStockException(productId, 1, 5));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"items\":"
                                + "[{\"productId\":\"" + productId + "\",\"quantity\":5}]}"))
                .andExpect(status().isConflict());
    }

    @Test
    void findAllShouldReturnAllOrFilterByStatus() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(orderFacade.findAll()).thenReturn(List.of(
                order(UUID.randomUUID(), customerId, productId, OrderStatus.NEW),
                order(UUID.randomUUID(), customerId, productId, OrderStatus.PAID)));
        when(orderFacade.findByStatus(OrderStatus.PAID)).thenReturn(List.of(
                order(UUID.randomUUID(), customerId, productId, OrderStatus.PAID)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/orders").param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PAID"));
    }

    @Test
    void findByIdAndByNumberShouldReturnOrderOr404() throws Exception {
        UUID id = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(orderFacade.findById(id)).thenReturn(order(id, customerId, productId, OrderStatus.NEW));
        when(orderFacade.findById(missing)).thenThrow(new OrderNotFoundException(missing));
        when(orderFacade.findByOrderNumber("CC-ABCDEF1234"))
                .thenReturn(order(id, customerId, productId, OrderStatus.NEW));

        mockMvc.perform(get("/api/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
        mockMvc.perform(get("/api/orders/" + missing))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/orders/by-number/CC-ABCDEF1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("CC-ABCDEF1234"));
    }

    @Test
    void updateStatusShouldReturnUpdatedOrderOr409() throws Exception {
        UUID id = UUID.randomUUID();
        UUID cancelled = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(orderFacade.updateStatus(id, OrderStatus.PAID))
                .thenReturn(order(id, customerId, productId, OrderStatus.PAID));
        when(orderFacade.updateStatus(cancelled, OrderStatus.PAID))
                .thenThrow(new IllegalStateException("Order is already cancelled."));

        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(patch("/api/orders/" + cancelled + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAID\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void updateStatusShouldReturn400WhenStatusMissing() throws Exception {
        mockMvc.perform(patch("/api/orders/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/orders/" + id))
                .andExpect(status().isNoContent());

        verify(orderFacade).delete(id);
    }

    @Test
    void illegalArgumentShouldBeMappedTo400() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(orderFacade.create(customerId, List.of(new OrderLine(productId, 1))))
                .thenThrow(new IllegalArgumentException("Order must contain at least one item."));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"items\":"
                                + "[{\"productId\":\"" + productId + "\",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest());
    }
}
