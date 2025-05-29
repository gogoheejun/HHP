package kr.hhplus.be.server.payment.dto;

import lombok.Data;

@Data
public class PayConcertReqDto {

    String amount;
    String method; //TOSS_PAY
    String pg_tid; //'T1234567890001' (토스에서 온 거래 ID)
    String scheduleId;
    String seatId;
    String reservationId;
}
