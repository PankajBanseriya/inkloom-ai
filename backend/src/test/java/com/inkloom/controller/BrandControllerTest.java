package com.inkloom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.config.CorsConfig;
import com.inkloom.config.SecurityConfig;
import com.inkloom.dto.request.BrandWorkspaceRequest;
import com.inkloom.dto.response.BrandGuidelineResponse;
import com.inkloom.dto.response.BrandWorkspaceResponse;
import com.inkloom.model.BrandGuideline;
import com.inkloom.model.BrandWorkspace;
import com.inkloom.service.BrandWorkspaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BrandWorkspaceService workspaceService;

    @Test
    @DisplayName("POST /api/v1/brands creates new workspace and returns 201 Created")
    void testCreateWorkspace() throws Exception {
        BrandWorkspaceRequest request = new BrandWorkspaceRequest("Acme Cloud", "Technology", "Cloud platform services");
        BrandWorkspace workspace = new BrandWorkspace("Acme Cloud", "acme-cloud", "Technology", "Cloud platform services", "user-1");
        workspace.setId("brand-acme");

        when(workspaceService.createWorkspace(any(BrandWorkspaceRequest.class), any()))
                .thenReturn(new BrandWorkspaceResponse(workspace));

        mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("brand-acme"))
                .andExpect(jsonPath("$.data.name").value("Acme Cloud"));
    }

    @Test
    @DisplayName("GET /api/v1/brands returns workspace list")
    void testGetAllWorkspaces() throws Exception {
        BrandWorkspace workspace = new BrandWorkspace("Acme Cloud", "acme-cloud", "Technology", "Cloud platform services", "user-1");
        workspace.setId("brand-acme");

        when(workspaceService.getAllWorkspaces()).thenReturn(List.of(new BrandWorkspaceResponse(workspace)));

        mockMvc.perform(get("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value("acme-cloud"));
    }

    @Test
    @DisplayName("GET /api/v1/brands/{brandId}/guidelines returns active guidelines")
    void testGetGuidelines() throws Exception {
        BrandGuideline guideline = new BrandGuideline("brand-acme", "v1.0", "user-1");
        guideline.setId("g-1");

        when(workspaceService.getGuideline("brand-acme")).thenReturn(new BrandGuidelineResponse(guideline));

        mockMvc.perform(get("/api/v1/brands/brand-acme/guidelines")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.version").value("v1.0"));
    }
}
