package kr.hhplus.be.server.reservation.presentation.dto;

import lombok.Data;

@Data
public class OneDateAvlSeatRespDto {
    String concertId;

    String scheduleId;

    String seatNumber;

    boolean isReserved;
}
