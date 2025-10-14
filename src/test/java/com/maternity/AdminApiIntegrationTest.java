package com.maternity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maternity.dto.AdminLoginRequest;
import com.maternity.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    public void setup() throws Exception {
        // Login as admin to get token
        AdminLoginRequest loginRequest = new AdminLoginRequest("admin", "admin123");

        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        adminToken = authResponse.getToken();
    }

    @Test
    public void testAdminLogin() throws Exception {
        AdminLoginRequest loginRequest = new AdminLoginRequest("admin", "admin123");

        mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userType").value("ADMIN"))
                .andExpect(jsonPath("$.user.username").value("admin"));
    }

    @Test
    public void testAdminLoginWithInvalidCredentials() throws Exception {
        AdminLoginRequest loginRequest = new AdminLoginRequest("admin", "wrongpassword");

        mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllOrders() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    public void testGetOrderById() throws Exception {
        mockMvc.perform(get("/api/admin/orders/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testGetOrderStats() throws Exception {
        mockMvc.perform(get("/api/admin/orders/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").exists())
                .andExpect(jsonPath("$.pendingOrders").exists())
                .andExpect(jsonPath("$.confirmedOrders").exists());
    }

    @Test
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    public void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/admin/users/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testGetMothers() throws Exception {
        mockMvc.perform(get("/api/admin/users/mothers")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetMatrons() throws Exception {
        mockMvc.perform(get("/api/admin/users/matrons")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetUserStats() throws Exception {
        mockMvc.perform(get("/api/admin/users/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.totalMothers").exists())
                .andExpect(jsonPath("$.totalMatrons").exists());
    }

    @Test
    public void testUpdateOrderStatus() throws Exception {
        String statusUpdate = "{\"status\": \"COMPLETED\"}";

        mockMvc.perform(put("/api/admin/orders/1/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    public void testSearchUsersByName() throws Exception {
        mockMvc.perform(get("/api/admin/users/search/name")
                .header("Authorization", "Bearer " + adminToken)
                .param("name", "Demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testUnauthorizedAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllMatronProfiles() throws Exception {
        mockMvc.perform(get("/api/admin/users/matron-profiles")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
