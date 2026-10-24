package com.inkloom.config;

import com.inkloom.repository.*;
import com.inkloom.security.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        JwtTokenProvider.class,
        CustomUserDetailsService.class
})
public class TestSecurityConfig {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BrandWorkspaceRepository brandWorkspaceRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @MockBean
    private ApprovedClaimRepository approvedClaimRepository;

    @MockBean
    private ApprovedSourceRepository approvedSourceRepository;

    @MockBean
    private RevisionRepository revisionRepository;

    @MockBean(name = "workspaceSecurity")
    private WorkspaceSecurityService workspaceSecurity;
}
