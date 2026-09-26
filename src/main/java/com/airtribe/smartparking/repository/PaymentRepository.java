package com.airtribe.smartparking.repository;

import com.airtribe.smartparking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByParkingTicketTicketNumber(String ticketNumber);

    Optional<Payment> findByParkingTicketId(Long parkingTicketId);

}
