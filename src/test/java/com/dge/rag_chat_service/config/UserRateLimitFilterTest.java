/*
package com.dge.rag_chat_service.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserRateLimitFilterTest {

    private UserRateLimitFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new UserRateLimitFilter();
        filterChain = mock(FilterChain.class);
    }

    //Request allowed within limit
    @Test
    void shouldAllowRequestWithinLimit() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        // Verify request passed through
        verify(filterChain, times(1)).doFilter(request, response);

        // Validate headers
        assertEquals("10", response.getHeader("X-RateLimit-Limit"));
        assertNotNull(response.getHeader("X-RateLimit-Remaining"));
        assertEquals(200, response.getStatus()); // default status
    }

    //Request blocked after exceeding limit
    @Test
    void shouldBlockRequestWhenLimitExceeded() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.0.1");

        // Exhaust tokens
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request,
                    new MockHttpServletResponse(),
                    mock(FilterChain.class));
        }

        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        filter.doFilter(request, blockedResponse, filterChain);

        // Should NOT call filter chain
        verify(filterChain, never()).doFilter(request, blockedResponse);

        // Validate 429
        assertEquals(429, blockedResponse.getStatus());
        assertEquals("0", blockedResponse.getHeader("X-RateLimit-Remaining"));
        assertNotNull(blockedResponse.getHeader("Retry-After"));
        assertTrue(blockedResponse.getContentAsString()
                .contains("Too Many Requests"));
    }
}
*/
