package com.cosmocats.marketplace;

import com.cosmocats.marketplace.presentation.dto.PagedResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagedResponseTest {

    @Test
    void constructorShouldInitializeAllFields() {
        List<String> content = Arrays.asList("item1", "item2");

        PagedResponse<String> response =
                new PagedResponse<>(content, 1, 10, 25L);

        assertEquals(content, response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(25L, response.getTotalElements());
    }

    @Test
    void getContentShouldReturnContent() {
        List<String> content = Arrays.asList("item1", "item2");
        PagedResponse<String> response =
                new PagedResponse<>(content, 0, 10, 2L);

        assertEquals(content, response.getContent());
    }

    @Test
    void setContentShouldUpdateContent() {
        PagedResponse<String> response =
                new PagedResponse<>(Collections.emptyList(), 0, 10, 0L);

        List<String> content = Arrays.asList("item1", "item2");
        response.setContent(content);

        assertEquals(content, response.getContent());
    }

    @Test
    void getPageShouldReturnPage() {
        PagedResponse<String> response =
                new PagedResponse<>(Collections.emptyList(), 2, 10, 0L);

        assertEquals(2, response.getPage());
    }

    @Test
    void setPageShouldUpdatePage() {
        PagedResponse<String> response =
                new PagedResponse<>(Collections.emptyList(), 0, 10, 0L);

        response.setPage(3);

        assertEquals(3, response.getPage());
    }

    @Test
    void getSizeShouldReturnSize() {
        PagedResponse<String> response =
                new PagedResponse<>(Collections.emptyList(), 0, 20, 0L);

        assertEquals(20, response.getSize());
    }

    @Test
    void setSizeShouldUpdateSize() {
        PagedResponse<String> response =
                new PagedResponse<>(Collections.emptyList(), 0, 10, 0L);

        response.setSize(50);

        assertEquals(50, response.getSize());
    }

    @Test
    void getTotalElementsShouldReturnTotalElements() {
        PagedResponse<String> response =
                new PagedResponse<>(Collections.emptyList(), 0, 10, 100L);

        assertEquals(100L, response.getTotalElements());
    }

    @Test
    void setTotalElementsShouldUpdateTotalElements() {
        PagedResponse<String> response =
                new PagedResponse<>(Collections.emptyList(), 0, 10, 0L);

        response.setTotalElements(250L);

        assertEquals(250L, response.getTotalElements());
    }

    @Test
    void constructorShouldSupportEmptyContent() {
        PagedResponse<String> response =
                new PagedResponse<>(Collections.emptyList(), 0, 10, 0L);

        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(0L, response.getTotalElements());
    }

    @Test
    void settersShouldSupportNullContent() {
        PagedResponse<String> response =
                new PagedResponse<>(
                        new ArrayList<>(), 0, 10, 0L
                );

        response.setContent(null);

        assertNull(response.getContent());
    }

    @Test
    void shouldSupportDifferentGenericTypes() {
        List<Integer> content = Arrays.asList(1, 2, 3);

        PagedResponse<Integer> response =
                new PagedResponse<>(content, 0, 3, 3L);

        assertEquals(content, response.getContent());
        assertEquals(Integer.valueOf(1), response.getContent().get(0));
    }
}
