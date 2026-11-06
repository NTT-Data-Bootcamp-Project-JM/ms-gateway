package com.nttdata.bank.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The gateway publishes the contract file that lives on the classpath.
 */
class OpenApiContractControllerTest {

    @Test
    void publishesTheSavedContract() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OpenApiContractController()).build();

        mockMvc.perform(get("/openapi.yaml"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("openapi:")));
    }
}
