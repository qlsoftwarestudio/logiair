package com.logiair.os.auth.dto;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

public class OnboardingRequestTest {

    @Test
    public void testDeserializeWithTaxId() throws Exception {
        String json = """
            {
                "businessName": "QuiloTradeSolutions",
                "taxId": "23-33877522-9",
                "adminName": "Victor Antu Quilodran",
                "adminEmail": "victorantuq@gmail.com",
                "adminPassword": "Quilotrade1001"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();
        OnboardingRequest request = objectMapper.readValue(json, OnboardingRequest.class);

        assertNotNull(request);
        assertEquals("QuiloTradeSolutions", request.getBusinessName());
        assertEquals("23-33877522-9", request.getTaxId());
        assertEquals("Victor Antu Quilodran", request.getAdminName());
        assertEquals("victorantuq@gmail.com", request.getAdminEmail());
        assertEquals("Quilotrade1001", request.getPassword());
    }

    @Test
    public void testSerializeWithTaxId() throws Exception {
        OnboardingRequest request = new OnboardingRequest();
        request.setBusinessName("QuiloTradeSolutions");
        request.setTaxId("23-33877522-9");
        request.setAdminName("Victor Antu Quilodran");
        request.setAdminEmail("victorantuq@gmail.com");
        request.setPassword("Quilotrade1001");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(request);

        assertTrue(json.contains("QuiloTradeSolutions"));
        assertTrue(json.contains("23-33877522-9"));
        assertTrue(json.contains("Victor Antu Quilodran"));
    }
}
