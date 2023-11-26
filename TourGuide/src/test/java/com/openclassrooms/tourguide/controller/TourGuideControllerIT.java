package com.openclassrooms.tourguide.controller;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import gpsUtil.GpsUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import rewardCentral.RewardCentral;

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
@AutoConfigureMockMvc
public class TourGuideControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    private void setUpPerTest() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @AfterEach
    private void afterPerTest() throws Exception {

    }

    // -----------------------------------------------------------------------------------------------
    // getNearbyAttractions method
    // -----------------------------------------------------------------------------------------------
    /**
     * HTTP GET /getNearbyAttractions, general case test, return HTTP 200
     */
//    private TourGuideService tourGuideService;

    @Test
    void getChildAlert_return204EmptyDatabase() throws Exception {
        // GIVEN
//        GpsUtil gpsUtil = new GpsUtil();
//        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
//        InternalTestHelper.setInternalUserNumber(1);
//        tourGuideService = new TourGuideService(gpsUtil, rewardsService);
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
