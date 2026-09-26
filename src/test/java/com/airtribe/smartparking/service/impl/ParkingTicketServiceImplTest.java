package com.airtribe.smartparking.service.impl;


import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.entity.Vehicle;
import com.airtribe.smartparking.enums.ParkingSpotStatus;
import com.airtribe.smartparking.enums.ParkingSpotType;
import com.airtribe.smartparking.enums.ParkingTicketStatus;
import com.airtribe.smartparking.enums.VehicleType;
import com.airtribe.smartparking.exception.ResourceNotFoundException;
import com.airtribe.smartparking.repository.ParkingTicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingTicketServiceImplTest {


    @InjectMocks
    private ParkingTicketServiceImpl parkingTicketService;

    @Mock
    private ParkingTicketRepository parkingTicketRepository;

    @Test
    void createParkingTicket_shouldCreateAndSaveTicket() {

        // Arrange

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingSpot parkingSpot = ParkingSpot.builder()
                .spotNumber("1-C1")
                .spotType(ParkingSpotType.COMPACT)
                .status(ParkingSpotStatus.OCCUPIED)
                .build();

        ParkingTicket savedTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .entryTime(LocalDateTime.now())
                .vehicle(vehicle)
                .parkingSpot(parkingSpot)
                .status(ParkingTicketStatus.ACTIVE)
                .build();

        when(parkingTicketRepository.save(any(ParkingTicket.class)))
                .thenReturn(savedTicket);


        // Act

        ParkingTicket result =
                parkingTicketService.createParkingTicket(vehicle, parkingSpot);


        // Assert

        assertNotNull(result);

        assertEquals("TKT-12345-ABCDE", result.getTicketNumber());
        assertEquals(vehicle, result.getVehicle());
        assertEquals(parkingSpot, result.getParkingSpot());
        assertEquals(ParkingTicketStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getEntryTime());

        verify(parkingTicketRepository, times(1))
                .save(any(ParkingTicket.class));
    }

    @Test
    void findByTicketNumber_shouldReturnTicketWhenTicketExists() {

        // Arrange

        String ticketNumber = "TKT-12345-ABCDE";

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber(ticketNumber)
                .vehicle(vehicle)
                .status(ParkingTicketStatus.ACTIVE)
                .entryTime(LocalDateTime.now())
                .build();

        when(parkingTicketRepository.findByTicketNumber(ticketNumber))
                .thenReturn(Optional.of(parkingTicket));


        // Act

        ParkingTicket result =
                parkingTicketService.findByTicketNumber(ticketNumber);


        // Assert

        assertNotNull(result);

        assertEquals(ticketNumber, result.getTicketNumber());
        assertEquals(vehicle, result.getVehicle());
        assertEquals(ParkingTicketStatus.ACTIVE, result.getStatus());

        verify(parkingTicketRepository, times(1))
                .findByTicketNumber(ticketNumber);
    }

    @Test
    void findByTicketNumber_shouldThrowExceptionWhenTicketDoesNotExist() {

        // Arrange

        String ticketNumber = "TKT-12345-ABCDE";

        when(parkingTicketRepository.findByTicketNumber(ticketNumber))
                .thenReturn(Optional.empty());


        // Act & Assert

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> parkingTicketService.findByTicketNumber(ticketNumber)
                );


        // Assert exception message

        assertEquals(
                "Ticket not found with number " + ticketNumber,
                exception.getMessage()
        );


        // Verify repository interaction

        verify(parkingTicketRepository, times(1))
                .findByTicketNumber(ticketNumber);
    }

    @Test
    void findActiveTicket_shouldReturnActiveTicket() {

        // Arrange

        String vehicleNumber = "MH27BD3354";

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber(vehicleNumber)
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicle(vehicle)
                .status(ParkingTicketStatus.ACTIVE)
                .entryTime(LocalDateTime.now())
                .build();

        when(parkingTicketRepository
                .findByVehicleVehicleNumberAndStatus(
                        vehicleNumber,
                        ParkingTicketStatus.ACTIVE
                ))
                .thenReturn(Optional.of(parkingTicket));


        // Act

        ParkingTicket result =
                parkingTicketService.findActiveTicket(vehicleNumber);


        // Assert

        assertNotNull(result);

        assertEquals(
                vehicleNumber,
                result.getVehicle().getVehicleNumber()
        );

        assertEquals(
                ParkingTicketStatus.ACTIVE,
                result.getStatus()
        );

        assertEquals(
                "TKT-12345-ABCDE",
                result.getTicketNumber()
        );


        // Verify repository interaction

        verify(parkingTicketRepository, times(1))
                .findByVehicleVehicleNumberAndStatus(
                        vehicleNumber,
                        ParkingTicketStatus.ACTIVE
                );
    }

    @Test
    void findActiveTicket_shouldThrowExceptionWhenActiveTicketDoesNotExist() {

        // Arrange

        String vehicleNumber = "MH27BD3354";

        when(parkingTicketRepository
                .findByVehicleVehicleNumberAndStatus(
                        vehicleNumber,
                        ParkingTicketStatus.ACTIVE
                ))
                .thenReturn(Optional.empty());


        // Act & Assert

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> parkingTicketService.findActiveTicket(vehicleNumber)
                );


        // Verify exception message

        assertEquals(
                "Active ticket not found for vehicle number " + vehicleNumber,
                exception.getMessage()
        );


        // Verify repository interaction

        verify(parkingTicketRepository, times(1))
                .findByVehicleVehicleNumberAndStatus(
                        vehicleNumber,
                        ParkingTicketStatus.ACTIVE
                );
    }


    @Test
    void closeParkingTicket_shouldSetExitTimeAndCompleteTicket() {

        // Arrange

        LocalDateTime entryTime =
                LocalDateTime.now().minusHours(2);

        LocalDateTime exitTime =
                LocalDateTime.now();

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .entryTime(entryTime)
                .status(ParkingTicketStatus.ACTIVE)
                .build();

        when(parkingTicketRepository.save(parkingTicket))
                .thenReturn(parkingTicket);


        // Act

        ParkingTicket result =
                parkingTicketService.closeParkingTicket(
                        parkingTicket,
                        exitTime
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                exitTime,
                result.getExitTime()
        );

        assertEquals(
                ParkingTicketStatus.COMPLETED,
                result.getStatus()
        );


        // Verify repository save

        verify(parkingTicketRepository, times(1))
                .save(parkingTicket);
    }

    @Test
    void save_shouldSaveAndReturnParkingTicket() {

        // Arrange

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .status(ParkingTicketStatus.COMPLETED)
                .build();

        when(parkingTicketRepository.save(parkingTicket))
                .thenReturn(parkingTicket);


        // Act

        ParkingTicket result =
                parkingTicketService.save(parkingTicket);


        // Assert

        assertNotNull(result);
        assertEquals(parkingTicket, result);

        verify(parkingTicketRepository, times(1))
                .save(parkingTicket);
    }
}
