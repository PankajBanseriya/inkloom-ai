package com.inkloom.service;

import com.inkloom.dto.request.ApprovedClaimRequest;
import com.inkloom.dto.response.ApprovedClaimResponse;
import com.inkloom.model.ApprovedClaim;
import com.inkloom.model.AuditEvent;
import com.inkloom.repository.ApprovedClaimRepository;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.BrandWorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovedClaimServiceTest {

    @Mock
    private ApprovedClaimRepository claimRepository;
    @Mock
    private BrandWorkspaceRepository workspaceRepository;
    @Mock
    private AuditEventRepository auditEventRepository;

    private ApprovedClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new ApprovedClaimService(claimRepository, workspaceRepository, auditEventRepository);
    }

    @Test
    @DisplayName("createClaim successfully persists claim and records audit event")
    void testCreateClaim() {
        String brandId = "brand-1";
        when(workspaceRepository.existsById(brandId)).thenReturn(true);

        ApprovedClaimRequest request = new ApprovedClaimRequest(
                "Acme delivers 99.99% uptime",
                "Acme SLA v4",
                "Global Enterprise",
                "Product Team",
                Instant.now().plusSeconds(864000)
        );

        when(claimRepository.save(any(ApprovedClaim.class))).thenAnswer(inv -> {
            ApprovedClaim c = inv.getArgument(0);
            c.setId("clm-100");
            return c;
        });

        ApprovedClaimResponse response = claimService.createClaim(brandId, request);

        assertNotNull(response);
        assertEquals("clm-100", response.getId());
        assertEquals("Acme delivers 99.99% uptime", response.getClaimText());
        assertFalse(response.isExpired());
        assertFalse(response.isEvidenceMissing());
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("updateClaimStatus changes claim status and records audit event")
    void testUpdateClaimStatus() {
        String claimId = "clm-100";
        ApprovedClaim claim = new ApprovedClaim("brand-1", "Latency under 5ms", "Benchmark Q2", "NA", "Owner", Instant.now().plusSeconds(3600));
        claim.setId(claimId);
        claim.setStatus("APPROVED");

        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(ApprovedClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovedClaimResponse response = claimService.updateClaimStatus("brand-1", claimId, "EXPIRED", "user-1");

        assertNotNull(response);
        assertEquals("EXPIRED", response.getStatus());
        assertTrue(response.isExpired());
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("deleteClaim deletes claim from repository and audits action")
    void testDeleteClaim() {
        String claimId = "clm-100";
        ApprovedClaim claim = new ApprovedClaim("brand-1", "Fastest db", "Benchmark Q1", "Global", "Owner", Instant.now().plusSeconds(3600));
        claim.setId(claimId);

        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        claimService.deleteClaim("brand-1", claimId, "user-admin");

        verify(claimRepository, times(1)).delete(claim);
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("ApprovedClaimResponse flags expired and missing evidence correctly")
    void testClaimFlags() {
        ApprovedClaim expiredClaim = new ApprovedClaim("brand-1", "100% security", null, "Global", "Owner", Instant.now().minusSeconds(3600));
        expiredClaim.setId("clm-exp");
        expiredClaim.setStatus("EXPIRED");

        ApprovedClaimResponse response = new ApprovedClaimResponse(expiredClaim);
        assertTrue(response.isExpired());
        assertTrue(response.isEvidenceMissing());
        assertTrue(response.getValidationNotes().contains("expired"));
    }
}
