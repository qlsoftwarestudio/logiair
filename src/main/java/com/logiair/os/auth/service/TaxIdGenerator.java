package com.logiair.os.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

@Service
public class TaxIdGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(TaxIdGenerator.class);
    
    // Counter for generating unique sequential IDs
    private final AtomicLong sequenceCounter = new AtomicLong(1);
    private final Random random = new Random();
    
    /**
     * Generates a unique tax ID in format XX-XXXXXXXX-X
     * Following Argentine CUIT format but clearly marked as auto-generated
     */
    public String generateTaxId(String businessName) {
        // Generate base number from timestamp and sequence
        long timestamp = System.currentTimeMillis() / 1000; // seconds since epoch
        long sequence = sequenceCounter.getAndIncrement();
        
        // Create 8-digit base number
        int baseNumber = (int) ((timestamp % 1000000) + (sequence % 1000));
        String base = String.format("%08d", baseNumber);
        
        // Generate verification digit
        int verificationDigit = calculateVerificationDigit(base);
        
        // Use prefix "99" to indicate auto-generated/temporary tax ID
        String taxId = String.format("99-%s-%d", base, verificationDigit);
        
        logger.info("Generated auto tax ID: {} for business: {}", taxId, businessName);
        return taxId;
    }
    
    /**
     * Calculates verification digit using modulo 11 algorithm (similar to CUIT)
     */
    private int calculateVerificationDigit(String base) {
        int[] multipliers = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        
        // For CUIT, we need 11 digits total: 2 prefix + 8 base + 1 verification
        // For 8-digit base, we need to prepend the prefix (2 digits) + type (1 digit)
        // But since we're only validating the base part, we'll use a standard approach
        
        String fullNumber;
        if (base.length() == 8) {
            // For 8-digit base, prepend with 2 digits to make it 10 digits for the algorithm
            fullNumber = "00" + base;  // This makes 10 digits
        } else {
            fullNumber = base;
        }
        
        // Ensure we don't exceed the multipliers array length
        int length = Math.min(fullNumber.length(), multipliers.length);
        
        for (int i = 0; i < length; i++) {
            int digit = Character.getNumericValue(fullNumber.charAt(i));
            sum += digit * multipliers[i];
        }
        
        int remainder = sum % 11;
        int verificationDigit = 11 - remainder;
        
        // Special cases for CUIT algorithm
        if (verificationDigit == 11) {
            verificationDigit = 0;
        } else if (verificationDigit == 10) {
            verificationDigit = 9;
        }
        
        return verificationDigit;
    }
    
    /**
     * Validates if a tax ID follows the expected format
     */
    public boolean isValidTaxId(String taxId) {
        if (taxId == null || !taxId.matches("^\\d{2}-\\d{8}-\\d$")) {
            return false;
        }
        
        String[] parts = taxId.split("-");
        if (parts.length != 3) {
            return false;
        }
        
        String base = parts[1];
        int expectedDigit = calculateVerificationDigit(base);
        int actualDigit = Integer.parseInt(parts[2]);
        
        return expectedDigit == actualDigit;
    }
    
    /**
     * Validates if a CUIT is valid for Argentine tax ID format
     */
    public boolean isValidCUIT(String cuit) {
        if (cuit == null || !cuit.matches("^\\d{2}-\\d{8}-\\d$")) {
            return false;
        }
        
        // Valid CUIT prefixes for Argentina
        String[] validPrefixes = {"20", "23", "24", "25", "26", "27", "30", "33", "34"};
        String prefix = cuit.substring(0, 2);
        
        boolean isValidPrefix = false;
        for (String validPrefix : validPrefixes) {
            if (prefix.equals(validPrefix)) {
                isValidPrefix = true;
                break;
            }
        }
        
        if (!isValidPrefix) {
            logger.warn("Invalid CUIT prefix: {}", prefix);
            return false;
        }
        
        return isValidTaxId(cuit);
    }
}
