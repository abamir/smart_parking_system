package com.airtribe.smartparking.controller;

import com.airtribe.smartparking.dto.request.CheckInRequest;
import com.airtribe.smartparking.dto.request.CheckOutRequest;
import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.enums.*;
import com.airtribe.smartparking.repository.ParkingSpotRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ParkingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;


    @Test
    void getParkingAvailability_shouldReturnAvailability() throws Exception {


        mockMvc.perform(
                        get("/api/v1/parking/availability")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.bikeAvailable").exists())
                .andExpect(jsonPath("$.data.compactAvailable").exists())
                .andExpect(jsonPath("$.data.largeAvailable").exists())
                .andExpect(jsonPath("$.data.busAvailable").exists());


    }

    @Test
    void checkIn_shouldCreateParkingTicket() throws Exception {


        CheckInRequest request = new CheckInRequest();
        request.setVehicleNumber("MH27BD3354");
        request.setOwnerName("Test User");
        request.setVehicleType(VehicleType.CAR);

        mockMvc.perform(
                        post("/api/v1/parking/check-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").exists())
                .andExpect(jsonPath("$.data.vehicleNumber").value("MH27BD3354"))
                .andExpect(jsonPath("$.data.vehicleType").value("CAR"))
                .andExpect(jsonPath("$.data.spotNumber").exists())
                .andExpect(jsonPath("$.data.floorNumber").exists())
                .andExpect(jsonPath("$.data.entryTime").exists())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

    }

    @Test
    void checkOut_shouldCompleteParkingAndRecordPayment() throws Exception {

        // 1. Check-in vehicle
        CheckInRequest checkInRequest = new CheckInRequest();
        checkInRequest.setVehicleNumber("MH27BD3355");
        checkInRequest.setOwnerName("Test User");
        checkInRequest.setVehicleType(VehicleType.CAR);


        MvcResult checkInResult = mockMvc.perform(
                        post("/api/v1/parking/check-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(checkInRequest))
                ).andExpect(status().isCreated())
                .andReturn();


        //// 2. Read ticket number from check-in Result
        JsonNode checkInResponse = objectMapper.readTree(checkInResult.getResponse().getContentAsString());

        String ticketNumber = checkInResponse.get("data").get("ticketNumber").asText();

        // 3. Prepare checkout request
        CheckOutRequest checkOutRequest = new CheckOutRequest();
        checkOutRequest.setTicketNumber(ticketNumber);
        checkOutRequest.setVehicleNumber("MH27BD3355");
        checkOutRequest.setPaymentMode(PaymentMode.UPI);
        checkOutRequest.setPaymentStatus(PaymentStatus.SUCCESS);

        // 4. Perform checkout
        mockMvc.perform(
                        post("/api/v1/parking/check-out")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(checkOutRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());


    }

    @Test
    void checkIn_withInvalidVehicleNumber_shouldReturnBadRequest() throws Exception {

        CheckInRequest request = new CheckInRequest();
        request.setVehicleNumber("");
        request.setOwnerName("Test User");
        request.setVehicleType(VehicleType.CAR);

        mockMvc.perform(
                        post("/api/v1/parking/check-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.errors.vehicleNumber").exists());
    }

    @Test
    void checkIn_withInvalidVehicleNumberFormat_shouldReturnBadRequest() throws Exception {

        CheckInRequest request = new CheckInRequest();
        request.setVehicleNumber("INVALID123");
        request.setOwnerName("Test User");
        request.setVehicleType(VehicleType.CAR);

        mockMvc.perform(
                        post("/api/v1/parking/check-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    void checkIn_whenVehicleAlreadyParked_shouldReturnInternalServerError() throws Exception {

        CheckInRequest request = new CheckInRequest();
        request.setVehicleNumber("MH27BD3354");
        request.setOwnerName("Test User");
        request.setVehicleType(VehicleType.CAR);

        // First check-in
        mockMvc.perform(
                post("/api/v1/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated());


        // Second check-in with the same vehicle

        mockMvc.perform(
                        post("/api/v1/parking/check-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Vehicle is already parked"));


    }


    @Test
    void checkIn_whenNoParkingSpotAvailable_shouldReturnConflict() throws Exception {

        // Arrange
        // Find all available COMPACT spots from the real test database

        List<ParkingSpot> availableSpots = parkingSpotRepository.findBySpotTypeAndStatus(
                ParkingSpotType.COMPACT,
                ParkingSpotStatus.AVAILABLE
        );

        assertFalse(availableSpots.isEmpty());

        // Make every COMPACT spot occupied

        availableSpots.forEach(
                spot ->
                        spot.setStatus(ParkingSpotStatus.OCCUPIED)

        );

        parkingSpotRepository.saveAll(availableSpots);

        CheckInRequest request = new CheckInRequest();

        request.setVehicleNumber("MH27XY9876");
        request.setOwnerName("Test User");
        request.setVehicleType(VehicleType.CAR);


        //Act  + assert

        mockMvc.perform(
                        post("/api/v1/parking/check-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));

    }


    void contextLoads() {
    }


}
