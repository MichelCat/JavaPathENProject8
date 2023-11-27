package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.NearByAttraction;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;

	private final ExecutorService executorService = Executors.newFixedThreadPool(100);

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;

		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user).join();
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
		CompletableFuture<VisitedLocation> completableFuture
				// Creating a VisitedLocation
				= CompletableFuture.supplyAsync(() -> {
					VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
					return visitedLocation;
				}, executorService)
				// Add VisitedLocation
				.thenApplyAsync(visitedLocation -> {
					user.addToVisitedLocations(visitedLocation);
					return visitedLocation;
				}, executorService)
				// Calculate rewards
				.thenApplyAsync(visitedLocation -> {
					rewardsService.calculateRewards(user).join();
					return visitedLocation;
				}, executorService);
		return completableFuture;
	}

	public void trackUserLocationForAllUsers(List<User> users) {
//		List<CompletableFuture<VisitedLocation>> futures = new ArrayList<>();
//		users.forEach(user -> {
//			futures.add(trackUserLocation(user));
//		});
//		CompletableFuture.allOf( futures.toArray(new CompletableFuture[futures.size()]) ).join();

		for (User user : users) {
			trackUserLocation(user);
		}
		shutdownExecutorService();
	}


	public void shutdownExecutorService() {
		// Stopping all running threads
		executorService.shutdown();

		try {
			// Waits indefinitely for all tasks to finish executing
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted");
		}
	}


	/**
	 * Get the closest five tourist attractions to the user - no matter how far away they are.
	 *
	 * @param visitedLocation Visited location
	 * @return List of five tourist attractions closest to the user
	 *
	 */
	public List<NearByAttraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<NearByAttraction> nearByAttractions = new ArrayList<NearByAttraction>();

		nearByAttractions = gpsUtil.getAttractions().stream()
				.map(attraction -> NearByAttraction.builder()
						.attractionName(attraction.attractionName)
						.attractionLatitude(attraction.latitude)
						.attractionLongitude(attraction.longitude)
						.userLatitude(visitedLocation.location.latitude)
						.userLongitude(visitedLocation.location.longitude)
						.distance(rewardsService.getDistance(attraction, visitedLocation.location))
						.rewardPoints(rewardsService.getRewardCentral().getAttractionRewardPoints(attraction.attractionId, visitedLocation.userId))
						.build())
				.sorted(Comparator.comparing(NearByAttraction::getDistance))
				.limit(5)
				.collect(Collectors.toList());

		return nearByAttractions;
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
