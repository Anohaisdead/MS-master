package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "moderatorUser", roles = {"MODERATOR"})
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @Test
    void helloTest() throws Exception {
        mockMvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "moderatorUser", roles = {"MODERATOR"})
    void createTest() throws Exception {
        UserRequest userRequest = new UserRequest("username", "email@mail.ru", "password", "firstName", "lastName");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());
        verify(userService,times(1)).createUser(userRequest);
    }

    @Test
    public void сreateTest_WithInvalidData() throws Exception{
        UserRequest userRequest =
                new UserRequest("1","email",
                        "123","","");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username should be between 2 and 30 characters long"))
                .andExpect(jsonPath("$.email").value("Email should be valid"))
                .andExpect(jsonPath("$.password").value("Password should be greater than 4 characters long"));
    }

    @Test
    void getUserByIdTest() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(
                "firstName","lastName","email@mail.ru",
                List.of("MODERATOR"),List.of("MODERATORS"));

        Mockito.when(userService.getUserById(uuid)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/{id}", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("firstName"))
                .andExpect(jsonPath("$.lastName").value("lastName"))
                .andExpect(jsonPath("$.email").value("email@mail.ru"))
                .andExpect(jsonPath("$.roles").value("MODERATOR"))
                .andExpect(jsonPath("$.groups").value("MODERATORS"));
        verify(userService,times(1)).getUserById(uuid);
    }

    @Test
    void getUserById_UserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        Mockito.when(userService.getUserById(userId)).thenThrow(new BackendResourcesException("User not found", HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }



}
