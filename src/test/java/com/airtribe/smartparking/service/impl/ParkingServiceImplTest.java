package com.airtribe.smartparking.service.impl;

import com.airtribe.smartparking.dto.request.CheckInRequest;
import com.airtribe.smartparking.dto.request.CheckOutRequest;
import com.airtribe.smartparking.dto.response.ParkingAvailabilityResponse;
import com.airtribe.smartparking.dto.response.ParkingTicketResponse;
import com.airtribe.smartparking.dto.response.PaymentResponse;
import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.entity.Payment;
import com.airtribe.smartparking.entity.Vehicle;
import com.airtribe.smartparking.enums.*;
import com.airtribe.smartparking.exception.ParkingSpotNotAvailableException;
import com.airtribe.smartparking.exception.TicketNotFoundException;
import com.airtribe.smartparking.exception.VehicleAlreadyParkedException;
import com.airtribe.smartparking.mapper.GenericMapper;
import com.airtribe.smartparking.mapper.ParkingTicketMapper;
import com.airtribe.smartparking.repository.ParkingSpotRepository;
import com.airtribe.smartparking.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceImplTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private ParkingSpotService parkingSpotService;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private ParkingTicketService parkingTicketService;

    @Mock
    private ParkingTicket parkingTicket;

    @Mock
    private FeeCalculationService feeCalculationService;

    @Mock
    private PaymentService paymentService;


    @InjectMocks
    private ParkingServiceImpl parkingService;

    @Mock
    private GenericMapper mapper;

    @Mock
    private ParkingTicketMapper parkingTicketMapper;


    @Test
    void getParkingAvailability_shouldReturnAvailability() {

        //Arrange
        ParkingAvailabilityResponse expectedParkingAvailabilityResponse =
                ParkingAvailabilityResponse.builder()
                        .bikeAvailable(8)
                        .compactAvailable(8)
                        .largeAvailable(8)
                        .busAvailable(8)
                        .build();

        when(parkingSpotService.getParkingAvailability())
                .thenReturn(expectedParkingAvailabilityResponse);

        //Act
        ParkingAvailabilityResponse actualParkingAvailabilityResponse =
                parkingService.getParkingAvailability();

        //Assert
        assertNotNull(actualParkingAvailabilityResponse);
        assertEquals(expectedParkingAvailabilityResponse, actualParkingAvailabilityResponse);

        verify(parkingSpotService, times(1)).getParkingAvailability();

    }

    @Test
    void checkIn_shouldSuccessfullyCreateParkingTicket() {
        //Arrange
        CheckInRequest request = CheckInRequest.builder()
                .vehicleNumber("MH27BD3354")
                .ownerName("Amir")
                .vehicleType(VehicleType.CAR)
                .build();


        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .ownerName("Amir")
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingSpot parkingSpot = ParkingSpot.builder()
                .spotNumber("1-C1")
                .spotType(ParkingSpotType.COMPACT)
                .status(ParkingSpotStatus.AVAILABLE)
                .build();


        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicle(vehicle)
                .parkingSpot(parkingSpot)
                .status(ParkingTicketStatus.ACTIVE)
                .build();

        ParkingTicketResponse expectedResponse =
                ParkingTicketResponse.builder()
                        .ticketNumber("TKT-12345-ABCDE")
                        .vehicleNumber("MH27BD3354")
                        .vehicleType(VehicleType.CAR)
                        .spotNumber("1-C1")
                        .status(ParkingTicketStatus.ACTIVE)
                        .build();


        // Vehicle is not already parked
        when(vehicleService.isVehicleAlreadyParked("MH27BD3354"))
                .thenReturn(false);

        // Vehicle already exists
        when(vehicleService.findVehicle("MH27BD3354"))
                .thenReturn(Optional.of(vehicle));

        // Parking spot allocation
        when(parkingSpotService.allocateParkingSpot(VehicleType.CAR))
                .thenReturn(parkingSpot);

        // Ticket creation
        when(parkingTicketService.createParkingTicket(vehicle, parkingSpot))
                .thenReturn(parkingTicket);

        // Ticket mapping
        when(parkingTicketMapper.toResponse(parkingTicket))
                .thenReturn(expectedResponse);

        // Act
        ParkingTicketResponse actualResponse =
                parkingService.checkIn(request);

        assertNotNull(actualResponse);

        assertEquals(
                "TKT-12345-ABCDE",
                actualResponse.getTicketNumber()
        );

        assertEquals(
                VehicleType.CAR,
                actualResponse.getVehicleType()
        );

        assertEquals(
                "1-C1",
                actualResponse.getSpotNumber()
        );

        assertEquals(
                ParkingTicketStatus.ACTIVE,
                actualResponse.getStatus()
        );

        // Verify service interactions

        verify(vehicleService, times(1))
                .isVehicleAlreadyParked("MH27BD3354");

        verify(vehicleService, times(1))
                .findVehicle("MH27BD3354");

        verify(parkingSpotService, times(1))
                .allocateParkingSpot(VehicleType.CAR);

        verify(parkingTicketService, times(1))
                .createParkingTicket(vehicle, parkingSpot);

        verify(parkingTicketMapper, times(1))
                .toResponse(parkingTicket);


    }


    @Test
    void checkIn_shouldThrowException_whenVehicleIsAlreadyParked() {

        // Arrange
        CheckInRequest request = CheckInRequest.builder()
                .vehicleNumber("MH27BD3354")
                .ownerName("Ab")
                .vehicleType(VehicleType.CAR)
                .build();

        when(vehicleService.isVehicleAlreadyParked("MH27BD3354"))
                .thenReturn(true);

        // Act & Assert
        VehicleAlreadyParkedException exception = assertThrows(
                VehicleAlreadyParkedException.class,
                () -> parkingService.checkIn(request)
        );


        // Verify exception message
        assertEquals(
                "Vehicle is already parked",
                exception.getMessage()
        );

        // Verify the process stopped

        verify(vehicleService, times(1))
                .isVehicleAlreadyParked("MH27BD3354");

        verify(vehicleService, never())
                .findVehicle(anyString());

        verify(parkingSpotService, never())
                .allocateParkingSpot(any());

        verify(parkingTicketService, never())
                .createParkingTicket(any(), any());

        verify(parkingTicketMapper, never())
                .toResponse(any());
    }


    @Test
    void checkIn_shouldThrowException_whenNoParkingSpotAvailable() {

        // Arrange

        CheckInRequest request = CheckInRequest.builder()
                .vehicleNumber("MH27BD3354")
                .ownerName("Ab")
                .vehicleType(VehicleType.CAR)
                .build();

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .ownerName("Ab")
                .vehicleType(VehicleType.CAR)
                .build();

        // Vehicle is not already parked
        when(vehicleService.isVehicleAlreadyParked("MH27BD3354"))
                .thenReturn(false);

        // Existing vehicle
        when(vehicleService.findVehicle("MH27BD3354"))
                .thenReturn(Optional.of(vehicle));

        // No parking spot available
        when(parkingSpotService.allocateParkingSpot(VehicleType.CAR))
                .thenThrow(
                        new ParkingSpotNotAvailableException(
                                "No parking spot available for vehicle type CAR"
                        )
                );


        // Act & Assert

        ParkingSpotNotAvailableException exception = assertThrows(
                ParkingSpotNotAvailableException.class,
                () -> parkingService.checkIn(request)
        );


        // Verify exception message

        assertEquals(
                "No parking spot available for vehicle type CAR",
                exception.getMessage()
        );


        // Verify the flow stopped

        verify(vehicleService, times(1))
                .isVehicleAlreadyParked("MH27BD3354");

        verify(vehicleService, times(1))
                .findVehicle("MH27BD3354");

        verify(parkingSpotService, times(1))
                .allocateParkingSpot(VehicleType.CAR);

        verify(parkingTicketService, never())
                .createParkingTicket(any(), any());

        verify(parkingTicketMapper, never())
                .toResponse(any());
    }

    @Test
    void checkIn_shouldCreateAndSaveVehicle_whenVehicleDoesNotExist() {

        // Arrange

        CheckInRequest request = CheckInRequest.builder()
                .vehicleNumber("MH27BD3354")
                .ownerName("Ab")
                .vehicleType(VehicleType.CAR)
                .build();

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .ownerName("Ab")
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingSpot parkingSpot = ParkingSpot.builder()
                .spotNumber("1-C1")
                .spotType(ParkingSpotType.COMPACT)
                .status(ParkingSpotStatus.OCCUPIED)
                .build();

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicle(vehicle)
                .parkingSpot(parkingSpot)
                .status(ParkingTicketStatus.ACTIVE)
                .build();

        ParkingTicketResponse expectedResponse =
                ParkingTicketResponse.builder()
                        .ticketNumber("TKT-12345-ABCDE")
                        .vehicleNumber("MH27BD3354")
                        .vehicleType(VehicleType.CAR)
                        .spotNumber("1-C1")
                        .status(ParkingTicketStatus.ACTIVE)
                        .build();


        // Vehicle is not already parked

        when(vehicleService.isVehicleAlreadyParked("MH27BD3354"))
                .thenReturn(false);


        // Vehicle does not exist

        when(vehicleService.findVehicle("MH27BD3354"))
                .thenReturn(Optional.empty());


        // Mapper creates Vehicle from CheckInRequest

        when(mapper.map(request, Vehicle.class))
                .thenReturn(vehicle);


        // Save newly created vehicle

        when(vehicleService.saveVehicle(vehicle))
                .thenReturn(vehicle);


        // Allocate parking spot

        when(parkingSpotService.allocateParkingSpot(VehicleType.CAR))
                .thenReturn(parkingSpot);


        // Create parking ticket

        when(parkingTicketService.createParkingTicket(vehicle, parkingSpot))
                .thenReturn(parkingTicket);


        // Map ticket to response

        when(parkingTicketMapper.toResponse(parkingTicket))
                .thenReturn(expectedResponse);


        // Act

        ParkingTicketResponse actualResponse =
                parkingService.checkIn(request);


        // Assert

        assertNotNull(actualResponse);

        assertEquals(
                "TKT-12345-ABCDE",
                actualResponse.getTicketNumber()
        );

        assertEquals(
                "MH27BD3354",
                actualResponse.getVehicleNumber()
        );

        assertEquals(
                VehicleType.CAR,
                actualResponse.getVehicleType()
        );


        // Verify new vehicle creation flow

        verify(vehicleService, times(1))
                .findVehicle("MH27BD3354");

        verify(mapper, times(1))
                .map(request, Vehicle.class);

        verify(vehicleService, times(1))
                .saveVehicle(vehicle);


        // Verify remaining check-in flow

        verify(parkingSpotService, times(1))
                .allocateParkingSpot(VehicleType.CAR);

        verify(parkingTicketService, times(1))
                .createParkingTicket(vehicle, parkingSpot);

        verify(parkingTicketMapper, times(1))
                .toResponse(parkingTicket);
    }

    @Test
    void checkOut_shouldSuccessfullyCompletePaymentAndReleaseSpot() {

        // Arrange

        CheckOutRequest request = CheckOutRequest.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicleNumber("MH27BD3354")
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        ParkingSpot parkingSpot = ParkingSpot.builder()
                .spotNumber("1-C1")
                .spotType(ParkingSpotType.COMPACT)
                .status(ParkingSpotStatus.OCCUPIED)
                .build();

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicle(vehicle)
                .parkingSpot(parkingSpot)
                .status(ParkingTicketStatus.ACTIVE)
                .entryTime(LocalDateTime.now().minusHours(2))
                .build();

        BigDecimal parkingFee = new BigDecimal("100.00");

        Payment payment = Payment.builder()
                .amount(parkingFee)
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .parkingTicket(parkingTicket)
                .build();

        PaymentResponse expectedResponse =
                PaymentResponse.builder()
                        .amount(parkingFee)
                        .paymentMode(PaymentMode.UPI)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .build();


        // Active ticket found

        when(parkingTicketService.findActiveTicket("MH27BD3354"))
                .thenReturn(parkingTicket);


        // Close ticket

        when(parkingTicketService.closeParkingTicket(
                eq(parkingTicket),
                any(LocalDateTime.class)
        )).thenReturn(parkingTicket);


        // Calculate fee

        when(feeCalculationService.calculateParkingFee(parkingTicket))
                .thenReturn(parkingFee);


        // Record payment

        when(paymentService.recordPayment(
                parkingTicket,
                parkingFee,
                PaymentMode.UPI,
                PaymentStatus.SUCCESS
        )).thenReturn(payment);


        // Map payment to response

        when(mapper.map(payment, PaymentResponse.class))
                .thenReturn(expectedResponse);


        // Act

        PaymentResponse actualResponse =
                parkingService.checkOut(request);


        // Assert

        assertNotNull(actualResponse);

        assertEquals(
                parkingFee,
                actualResponse.getAmount()
        );

        assertEquals(
                PaymentMode.UPI,
                actualResponse.getPaymentMode()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                actualResponse.getPaymentStatus()
        );


        // Verify complete checkout flow

        verify(parkingTicketService, times(1))
                .findActiveTicket("MH27BD3354");

        verify(parkingTicketService, times(1))
                .closeParkingTicket(
                        eq(parkingTicket),
                        any(LocalDateTime.class)
                );

        verify(feeCalculationService, times(1))
                .calculateParkingFee(parkingTicket);

        verify(parkingTicketService, times(1))
                .save(parkingTicket);

        verify(paymentService, times(1))
                .recordPayment(
                        parkingTicket,
                        parkingFee,

                        PaymentMode.UPI,
                        PaymentStatus.SUCCESS
                );

        verify(parkingSpotService, times(1))
                .releaseParkingSpot(parkingSpot.getId());

        verify(mapper, times(1))
                .map(payment, PaymentResponse.class);
    }

    @Test
    void checkOut_shouldNotReleaseSpot_whenPaymentFails() {

        // Arrange

        CheckOutRequest request = CheckOutRequest.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicleNumber("MH27BD3354")
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.FAILED)
                .build();

        ParkingSpot parkingSpot = ParkingSpot.builder()
                .spotNumber("1-C1")
                .spotType(ParkingSpotType.COMPACT)
                .status(ParkingSpotStatus.OCCUPIED)
                .build();

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicle(vehicle)
                .parkingSpot(parkingSpot)
                .status(ParkingTicketStatus.ACTIVE)
                .entryTime(LocalDateTime.now().minusHours(2))
                .build();

        BigDecimal parkingFee = new BigDecimal("100.00");

        Payment payment = Payment.builder()
                .amount(parkingFee)
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.FAILED)
                .paidAt(LocalDateTime.now())
                .parkingTicket(parkingTicket)
                .build();

        PaymentResponse expectedResponse =
                PaymentResponse.builder()
                        .amount(parkingFee)
                        .paymentMode(PaymentMode.UPI)
                        .paymentStatus(PaymentStatus.FAILED)
                        .build();


        // Active ticket found

        when(parkingTicketService.findActiveTicket("MH27BD3354"))
                .thenReturn(parkingTicket);


        // Close ticket

        when(parkingTicketService.closeParkingTicket(
                eq(parkingTicket),
                any(LocalDateTime.class)
        )).thenReturn(parkingTicket);


        // Calculate fee

        when(feeCalculationService.calculateParkingFee(parkingTicket))
                .thenReturn(parkingFee);


        // Record failed payment

        when(paymentService.recordPayment(
                parkingTicket,
                parkingFee,
                PaymentMode.UPI,
                PaymentStatus.FAILED
        )).thenReturn(payment);


        // Map payment response

        when(mapper.map(payment, PaymentResponse.class))
                .thenReturn(expectedResponse);


        // Act

        PaymentResponse actualResponse =
                parkingService.checkOut(request);


        // Assert

        assertNotNull(actualResponse);

        assertEquals(
                parkingFee,
                actualResponse.getAmount()
        );

        assertEquals(
                PaymentStatus.FAILED,
                actualResponse.getPaymentStatus()
        );


        // Verify checkout operations

        verify(parkingTicketService, times(1))
                .findActiveTicket("MH27BD3354");

        verify(parkingTicketService, times(1))
                .closeParkingTicket(
                        eq(parkingTicket),
                        any(LocalDateTime.class)
                );

        verify(feeCalculationService, times(1))
                .calculateParkingFee(parkingTicket);

        verify(parkingTicketService, times(1))
                .save(parkingTicket);

        verify(paymentService, times(1))
                .recordPayment(
                        parkingTicket,
                        parkingFee,
                        PaymentMode.UPI,
                        PaymentStatus.FAILED
                );


        // VERY IMPORTANT:
        // Parking spot must NOT be released

        verify(parkingSpotService, never())
                .releaseParkingSpot(any());
    }

    @Test
    void checkOut_shouldThrowException_whenActiveTicketNotFound() {

        // Arrange

        CheckOutRequest request = CheckOutRequest.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicleNumber("MH27BD3354")
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        when(parkingTicketService.findActiveTicket("MH27BD3354"))
                .thenThrow(
                        new TicketNotFoundException(
                                "Active ticket not found for vehicle number MH27BD3354"
                        )
                );


        // Act & Assert

        TicketNotFoundException exception = assertThrows(
                TicketNotFoundException.class,
                () -> parkingService.checkOut(request)
        );


        // Verify exception message

        assertEquals(
                "Active ticket not found for vehicle number MH27BD3354",
                exception.getMessage()
        );


        // Verify ticket lookup happened

        verify(parkingTicketService, times(1))
                .findActiveTicket("MH27BD3354");


        // Verify checkout stopped immediately

        verify(parkingTicketService, never())
                .closeParkingTicket(any(), any());

        verify(feeCalculationService, never())
                .calculateParkingFee(any());

        verify(parkingTicketService, never())
                .save(any());

        verify(paymentService, never())
                .recordPayment(
                        any(),
                        any(),
                        any(),
                        any()
                );

        verify(parkingSpotService, never())
                .releaseParkingSpot(any());

        verify(mapper, never())
                .map(any(), eq(PaymentResponse.class));
    }

    @Test
    void checkOut_shouldCalculateAndSaveCorrectParkingFee() {

        // Arrange

        CheckOutRequest request = CheckOutRequest.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicleNumber("MH27BD3354")
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        ParkingSpot parkingSpot = ParkingSpot.builder()
                .spotNumber("1-C1")
                .spotType(ParkingSpotType.COMPACT)
                .status(ParkingSpotStatus.OCCUPIED)
                .build();

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicle(vehicle)
                .parkingSpot(parkingSpot)
                .status(ParkingTicketStatus.ACTIVE)
                .entryTime(LocalDateTime.now().minusHours(3))
                .build();

        BigDecimal expectedFee = new BigDecimal("150.00");

        Payment payment = Payment.builder()
                .amount(expectedFee)
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .parkingTicket(parkingTicket)
                .build();

        PaymentResponse expectedResponse =
                PaymentResponse.builder()
                        .amount(expectedFee)
                        .paymentMode(PaymentMode.UPI)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .build();


        // Active ticket

        when(parkingTicketService.findActiveTicket("MH27BD3354"))
                .thenReturn(parkingTicket);


        // Close ticket

        when(parkingTicketService.closeParkingTicket(
                eq(parkingTicket),
                any(LocalDateTime.class)
        )).thenReturn(parkingTicket);


        // Fee calculation

        when(feeCalculationService.calculateParkingFee(parkingTicket))
                .thenReturn(expectedFee);


        // Payment

        when(paymentService.recordPayment(
                parkingTicket,
                expectedFee,
                PaymentMode.UPI,
                PaymentStatus.SUCCESS
        )).thenReturn(payment);


        // Response mapping

        when(mapper.map(payment, PaymentResponse.class))
                .thenReturn(expectedResponse);


        // Act

        PaymentResponse actualResponse =
                parkingService.checkOut(request);


        // Assert

        assertNotNull(actualResponse);

        assertEquals(
                expectedFee,
                actualResponse.getAmount()
        );


        // Capture the ParkingTicket passed to save()

        ArgumentCaptor<ParkingTicket> ticketCaptor =
                ArgumentCaptor.forClass(ParkingTicket.class);

        verify(parkingTicketService, times(1))
                .save(ticketCaptor.capture());


        // Get the actual ticket that was saved

        ParkingTicket savedTicket =
                ticketCaptor.getValue();


        // Verify correct fee was stored

        assertEquals(
                expectedFee,
                savedTicket.getTotalFee()
        );


        // Verify fee calculation happened exactly once

        verify(feeCalculationService, times(1))
                .calculateParkingFee(parkingTicket);
    }

    @Test
    void checkOut_shouldCloseActiveTicketBeforeCalculatingFee() {

        // Arrange

        CheckOutRequest request = CheckOutRequest.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicleNumber("MH27BD3354")
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        ParkingSpot parkingSpot = ParkingSpot.builder()
                .spotNumber("1-C1")
                .spotType(ParkingSpotType.COMPACT)
                .status(ParkingSpotStatus.OCCUPIED)
                .build();

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber("MH27BD3354")
                .vehicleType(VehicleType.CAR)
                .build();

        ParkingTicket parkingTicket = ParkingTicket.builder()
                .ticketNumber("TKT-12345-ABCDE")
                .vehicle(vehicle)
                .parkingSpot(parkingSpot)
                .status(ParkingTicketStatus.ACTIVE)
                .entryTime(LocalDateTime.now().minusHours(2))
                .build();

        BigDecimal parkingFee = new BigDecimal("100.00");

        Payment payment = Payment.builder()
                .amount(parkingFee)
                .paymentMode(PaymentMode.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .parkingTicket(parkingTicket)
                .build();

        PaymentResponse expectedResponse =
                PaymentResponse.builder()
                        .amount(parkingFee)
                        .paymentMode(PaymentMode.UPI)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .build();


        when(parkingTicketService.findActiveTicket("MH27BD3354"))
                .thenReturn(parkingTicket);

        when(parkingTicketService.closeParkingTicket(
                eq(parkingTicket),
                any(LocalDateTime.class)
        )).thenReturn(parkingTicket);

        when(feeCalculationService.calculateParkingFee(parkingTicket))
                .thenReturn(parkingFee);

        when(paymentService.recordPayment(
                parkingTicket,
                parkingFee,
                PaymentMode.UPI,
                PaymentStatus.SUCCESS
        )).thenReturn(payment);

        when(mapper.map(payment, PaymentResponse.class))
                .thenReturn(expectedResponse);


        // Act

        parkingService.checkOut(request);


        // Assert

        ArgumentCaptor<LocalDateTime> exitTimeCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        verify(parkingTicketService, times(1))
                .closeParkingTicket(
                        eq(parkingTicket),
                        exitTimeCaptor.capture()
                );


        LocalDateTime capturedExitTime =
                exitTimeCaptor.getValue();

        assertNotNull(capturedExitTime);


        // Verify fee calculation happens using the closed ticket

        verify(feeCalculationService, times(1))
                .calculateParkingFee(parkingTicket);
    }
}
