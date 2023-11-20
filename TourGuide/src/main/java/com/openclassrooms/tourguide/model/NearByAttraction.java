package com.openclassrooms.tourguide.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Near by attraction is business model
 *
 * @author MC
 * @version 1.0
 */

@Data
@FieldDefaults(level= AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NearByAttraction {
    /**
     * Name of Tourist attraction,
     */
    @NotBlank
    String attractionName;
    /**
     * Tourist attraction latitude
     */
    @NotNull
    double attractionLatitude;
    /**
     * Tourist attraction longitude
     */
    @NotNull
    double attractionLongitude;
    /**
     * User location latitude
     */
    @NotNull
    double userLatitude;
    /**
     * User location longitude
     */
    @NotNull
    double userLongitude;
    /**
     * The distance in miles between the user's location and each of the attractions
     */
    @NotNull
    double distance;
    /**
     * Reward points for visiting the attraction
     */
    @NotNull
    int rewardPoints;
}
