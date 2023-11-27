package com.openclassrooms.tourguide.controller;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TourGuideControllerIT is a class of Endpoint integration tests on TourGuideController
 *
 * @author MC
 * @version 1.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
//@AutoConfigureMockMvc
public class TourGuideControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    private void setUpBeforeEach() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @AfterEach
    private void cleanUpAfterEach() throws Exception {
        InternalTestHelper.setInternalUserNumber(100);
    }

    // -----------------------------------------------------------------------------------------------
    // getNearbyAttractions method
    // -----------------------------------------------------------------------------------------------
    /**
     * HTTP GET /getNearbyAttractions, general case test, return HTTP 200
     */
    @Test
    void getChildAlert_return204EmptyDatabase() throws Exception {
        // GIVEN
        // WHEN
        mockMvc.perform(get("/getNearbyAttractions")
                        .param("userName", "internalUser1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].attractionName").isNotEmpty())
                .andExpect(jsonPath("$[*].attractionLatitude").isNotEmpty())
                .andExpect(jsonPath("$[*].attractionLongitude").isNotEmpty())
                .andExpect(jsonPath("$[*].userLatitude").isNotEmpty())
                .andExpect(jsonPath("$[*].userLongitude").isNotEmpty())
                .andExpect(jsonPath("$[*].distance").isNotEmpty())
                .andExpect(jsonPath("$[*].rewardPoints").isNotEmpty())
                .andExpect(jsonPath("$", hasSize(5)))
                .andDo(print());
        // THEN
    }

}
