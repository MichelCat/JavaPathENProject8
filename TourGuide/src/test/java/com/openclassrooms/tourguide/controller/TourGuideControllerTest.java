package com.openclassrooms.tourguide.controller;

import com.openclassrooms.tourguide.TourGuideController;
import com.openclassrooms.tourguide.model.NearByAttraction;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TourGuideControllerTest is a class of Endpoint unit tests on TourGuideController
 *
 * @author MC
 * @version 1.0
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@WebMvcTest(controllers = TourGuideController.class)
public class TourGuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private TourGuideService tourGuideService;

    @BeforeEach
    public void setUpBefore() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    // -----------------------------------------------------------------------------------------------
    // getNearbyAttractions method
    // -----------------------------------------------------------------------------------------------
    /**
     * HTTP GET /getNearbyAttractions, general case test, return HTTP 200
     */
    @Test
    void getNearbyAttractions_return200() throws Exception {
        // GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        when(tourGuideService.getUser(any(String.class))).thenReturn(user);

        Attraction attraction = new Attraction("Disneyland", "Anaheim", "CA", 33.817595, -117.922008);
        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), attraction, new Date());
        when(tourGuideService.getUserLocation(any(User.class))).thenReturn(visitedLocation);

        List<NearByAttraction> nearByAttractions = new ArrayList<NearByAttraction>();
        nearByAttractions.add(new NearByAttraction("Roger Dean Stadium", 26.890959, -80.116577, -41.490102, -13.738881, 6340.588384814216, 32));
        nearByAttractions.add(new NearByAttraction("Cinderella Castle", 28.419411, -81.5812, -41.490102, -13.738881, 6478.767525819528, 153));
        nearByAttractions.add(new NearByAttraction("Zoo Tampa at Lowry Park", 28.012804, -82.469269, -41.490102, -13.738881, 6496.343059725859, 362));
        nearByAttractions.add(new NearByAttraction("Franklin Park Zoo", 42.302601, -71.086731, -41.490102, -13.738881, 6797.773864049046, 471));
        nearByAttractions.add(new NearByAttraction("Flatiron Building", 40.741112, -73.989723, -41.490102, -13.738881, 6812.87693955952, 362));

        when(tourGuideService.getNearByAttractions(any(VisitedLocation.class))).thenReturn(nearByAttractions);
        // WHEN
        mockMvc.perform(get("/getNearbyAttractions")
                        .param("userName", "internalUser1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attractionName").value("Roger Dean Stadium"))
                .andExpect(jsonPath("$[1].attractionName").value("Cinderella Castle"))
                .andExpect(jsonPath("$[2].attractionName").value("Zoo Tampa at Lowry Park"))
                .andExpect(jsonPath("$[3].attractionName").value("Franklin Park Zoo"))
                .andExpect(jsonPath("$[4].attractionName").value("Flatiron Building"))
                .andExpect(jsonPath("$[*].attractionLatitude").isNotEmpty())
                .andExpect(jsonPath("$[*].attractionLongitude").isNotEmpty())
                .andExpect(jsonPath("$[*].userLatitude").isNotEmpty())
                .andExpect(jsonPath("$[*].userLongitude").isNotEmpty())
                .andExpect(jsonPath("$[*].distance").isNotEmpty())
                .andExpect(jsonPath("$[*].rewardPoints").isNotEmpty())
                .andExpect(jsonPath("$", hasSize(5)))
                .andDo(print());
        // THEN
        verify(tourGuideService, Mockito.times(1)).getNearByAttractions(any(VisitedLocation.class));
    }


}
