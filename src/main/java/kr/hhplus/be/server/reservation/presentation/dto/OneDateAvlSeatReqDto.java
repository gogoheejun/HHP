package kr.hhplus.be.server.reservation.presentation.dto;

import lombok.Data;

@Data
public class OneDateAvlSeatReqDto {

    String waitToken;

    String scheduleDate;

    String concertId;
}
