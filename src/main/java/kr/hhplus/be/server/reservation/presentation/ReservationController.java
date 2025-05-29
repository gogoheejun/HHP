package kr.hhplus.be.server.reservation.presentation;

import kr.hhplus.be.server.reservation.presentation.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/reservation")
public class ReservationController {


    /**
     * 예약 가능일자 조회
     */
    @PostMapping("/avlDates")
    public ResponseEntity<List<SeatAvlDatesRespDto>> getAvlDates(@RequestBody SeatAvlDatesReqDto reqDto) {
        return ResponseEntity.ok(List.of());
    }

    /**
     * 예약대기열 토큰 요청
     */
    @PostMapping("/waitToken")
    public ResponseEntity<WaitTokenRespDto> getToken(@RequestBody WaitTokenReqDto waitTokenReqDto) {
        // scheduleId에 따라 토큰순위 다르게 부여
        // 같은 scheduleId에 동일한 사용자가 중복요청 시 기존 토큰응답
        return ResponseEntity.ok(new WaitTokenRespDto("tempToken"));
    }

    /**
     * 해당일자의 예약가능 좌석 조회
     * 단, 토큰이 유효한 사람만 조회가능.(= 대기열 순서인 사람들)
     */
    @PostMapping("/oneDateAvlSeats")
    public ResponseEntity<OneDateAvlSeatRespDto> getOneDateAvlSeats(@RequestBody OneDateAvlSeatReqDto reqDto) {
        return ResponseEntity.ok(new OneDateAvlSeatRespDto());
    }

    /**
     * 좌석 예약
     */
    @PostMapping("/lock")
    public ResponseEntity<Boolean> lockSeat(@RequestBody SeatLockReqDto reqDto) {
        boolean success = true; //todo 락설정
        return ResponseEntity.ok(true);
    }
}
