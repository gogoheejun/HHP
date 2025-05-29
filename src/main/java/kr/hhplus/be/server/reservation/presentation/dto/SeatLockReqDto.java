package kr.hhplus.be.server.reservation.presentation.dto;

import lombok.Data;

@Data
public class SeatLockReqDto {
    String waitToken;
    String scheduleId;
}
