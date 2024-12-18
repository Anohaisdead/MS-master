package com.itm.space.backendresources.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "MODERATOR")
public class UserServiceTest extends BaseIntegrationTest {

    private final UserService userService;
    private final Keycloak keycloak;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    public UserServiceTest(Keycloak keycloak, UserService userService) {
        this.keycloak = keycloak;
        this.userService = userService;
    }

    UserRequest userRequest = new UserRequest("username","email@mail.com","password","firstName","lastName");

    private void removeUserIfExists(String username) {
        List<UserRepresentation> users = keycloak.realm("ITM").users().search(userRequest.getUsername());
        if (!users.isEmpty()) {
            keycloak.realm("ITM").users().get(users.get(0).getId()).remove();
        }
    }

    @BeforeEach
    void setUp() {
        removeUserIfExists(userRequest.getUsername());
    }

    @AfterEach
    void tearDown() {
        removeUserIfExists(userRequest.getUsername());
    }

    @Test
    void createUserTest() throws Exception {
        mockMvc.perform(requestWithContent(post("/api/users"),userRequest)).andExpect(status().isOk());
        UserRepresentation newUser = keycloak.realm("ITM").users().search(userRequest.getUsername()).get(0);
        assertEquals(userRequest.getUsername().toLowerCase(), newUser.getUsername().toLowerCase());
        assertEquals(userRequest.getEmail().toLowerCase(), newUser.getEmail().toLowerCase());
        assertEquals(userRequest.getLastName().toLowerCase(), newUser.getLastName().toLowerCase());
        assertEquals(userRequest.getFirstName().toLowerCase(), newUser.getFirstName().toLowerCase());
        keycloak.realm("ITM").users().get(newUser.getId()).remove();
    }

    @Test
    void getUserByIdTest() throws Exception {
        mockMvc.perform(requestWithContent(post("/api/users"),userRequest)).andExpect(status().isOk());
        UserRepresentation newUser = keycloak.realm("ITM").users().search(userRequest.getUsername()).get(0);
        UserResponse userResponse = userService.getUserById(UUID.fromString(newUser.getId()));
        assertNotNull(userResponse);
        assertEquals(userRequest.getUsername().toLowerCase(), newUser.getUsername().toLowerCase());
        assertEquals(userRequest.getEmail().toLowerCase(), newUser.getEmail().toLowerCase());
        assertEquals(userRequest.getLastName().toLowerCase(), newUser.getLastName().toLowerCase());
        assertEquals(userRequest.getFirstName().toLowerCase(), newUser.getFirstName().toLowerCase());
        keycloak.realm("ITM").users().get(newUser.getId()).remove();
    }

}
