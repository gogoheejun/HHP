package kr.hhplus.be.server.payment;

import kr.hhplus.be.server.payment.dto.PayConcertReqDto;
import kr.hhplus.be.server.payment.dto.PayConcertRespDto;
import kr.hhplus.be.server.reservation.presentation.dto.SeatAvlDatesReqDto;
import kr.hhplus.be.server.reservation.presentation.dto.SeatAvlDatesRespDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    /**
     * 콘서트결재
     */
    @PostMapping("/concert")
    public ResponseEntity<PayConcertRespDto> payConcert(@RequestBody PayConcertReqDto reqDto) {
        return ResponseEntity.ok(new PayConcertRespDto("10000","TOSSPAY","111","10","55"));
    }
}
