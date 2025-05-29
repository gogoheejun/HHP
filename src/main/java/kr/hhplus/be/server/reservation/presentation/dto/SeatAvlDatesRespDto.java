package kr.hhplus.be.server.reservation.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvlDatesRespDto {
    String concertId;

    String concertScheduleDate;

    String concertReserveStartTime;

    String concertReserveEndTime;

    String hasAvlSeats;
}
