package com.airtribe.smartparking.config;

import com.airtribe.smartparking.entity.ParkingFloor;
import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.enums.FloorStatus;
import com.airtribe.smartparking.enums.ParkingSpotStatus;
import com.airtribe.smartparking.enums.ParkingSpotType;
import com.airtribe.smartparking.repository.ParkingFloorRepository;
import com.airtribe.smartparking.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final ParkingFloorRepository parkingFloorRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    @Override
    public void run(String... args) {

        if (parkingFloorRepository.count() > 0) {
            log.info("Parking data already initialized.");
            return;
        }

        log.info("Initializing parking data...");

        initializeParkingData();

        log.info("Parking data initialized successfully.");
    }

    private void initializeParkingData() {

        ParkingFloor floor1 = createFloor(1, "Ground Floor");
        ParkingFloor floor2 = createFloor(2, "First Floor");

        createParkingSpots(floor1);
        createParkingSpots(floor2);
    }

    private ParkingFloor createFloor(
            Integer floorNumber,
            String floorName
    ) {

        ParkingFloor floor = ParkingFloor.builder()
                .floorNumber(floorNumber)
                .floorName(floorName)
                .status(FloorStatus.ACTIVE)
                .build();

        return parkingFloorRepository.save(floor);
    }

    private void createParkingSpots(ParkingFloor floor) {

        createSpot(floor, ParkingSpotType.BIKE, "B", 4);
        createSpot(floor, ParkingSpotType.COMPACT, "C", 4);
        createSpot(floor, ParkingSpotType.LARGE, "L", 4);
        createSpot(floor, ParkingSpotType.BUS, "BS", 2);
    }

    private void createSpot(
            ParkingFloor floor,
            ParkingSpotType spotType,
            String prefix,
            int count
    ) {

        for (int i = 1; i <= count; i++) {

            ParkingSpot parkingSpot = ParkingSpot.builder()
                    .spotNumber(
                            floor.getFloorNumber()
                                    + "-"
                                    + prefix
                                    + i
                    )
                    .spotType(spotType)
                    .status(ParkingSpotStatus.AVAILABLE)
                    .parkingFloor(floor)
                    .build();

            parkingSpotRepository.save(parkingSpot);
        }
    }
}
