package kr.hhplus.be.server.payment.dto;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class PayConcertRespDto {

    String amount;
    String method; //TOSS_PAY
    String scheduleId;
    String seatId;
    String reservationId;
}
