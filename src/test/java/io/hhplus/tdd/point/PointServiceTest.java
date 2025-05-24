package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Any;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class PointServiceTest {

    @Test
    @DisplayName("ID로 유저의 포인트를 조회하면 해당 포인트를 반환한다.")
    void getUserPoint(){

        // given
        UserPointTable stubUserTable = mock(UserPointTable.class);
        PointHistoryTable stubPointHistoryTable = mock(PointHistoryTable.class);
        PointService pointService = new PointService(stubUserTable, stubPointHistoryTable);

        when(stubUserTable.selectById(10L)).thenReturn(new UserPoint(10L, 100L, System.currentTimeMillis() ));

        // when
        UserPoint userPoint = pointService.getUserPoint(10L);

        // then
        assertNotNull(userPoint);
        assertEquals(userPoint.point(), 100L);
    }

    @Test
    @DisplayName("ID로 유저의 포인트 충전/이용 내역을 조회하면, 모든 기록을 반환한다.")
    void getHistory(){
        // given
        UserPointTable stubUserTable = mock(UserPointTable.class);
        PointHistoryTable stubPointHistoryTable = mock(PointHistoryTable.class);
        PointService pointService = new PointService(stubUserTable, stubPointHistoryTable);

        when(stubPointHistoryTable.selectAllByUserId(10L)).thenReturn(
                List.of(
                        new PointHistory(1L, 10L, 1000L, CHARGE, System.currentTimeMillis()),
                        new PointHistory(2L, 11L, 500L, USE, System.currentTimeMillis())
                        ));

        // when
        List<PointHistory> pointHistoryList = pointService.getHistory(10L);

        // then
        assertNotNull(pointHistoryList);
        assertEquals(pointHistoryList.size(), 2);
        assertEquals(pointHistoryList.get(0).amount(), 1000L);
        assertEquals(pointHistoryList.get(0).type(), CHARGE);
        assertEquals(pointHistoryList.get(1).amount(), 500L);
        assertEquals(pointHistoryList.get(1).type(), USE);
    }

    @Test
    @DisplayName("id에 해당하는 유저에 amount만큼 포인트를 충전하면 해당액수만큼 보유 포인트가 증가한다.")
    void charge(){
        /***
         * 기존 500포인트 보유하고 있는 고객이 1000포인트를 충전하면 1500포인트가 된다.
         */
        // given
        UserPointTable stubUserPointTable = mock(UserPointTable.class);
        PointHistoryTable stubHistoryTable = mock(PointHistoryTable.class);
        PointService pointService = new PointService(stubUserPointTable, stubHistoryTable);
        // 기존 500포인트 보유하고 있음
        when(stubUserPointTable.selectById(10L)).thenReturn(new UserPoint(10L, 500L, System.currentTimeMillis()));
        when(stubUserPointTable.insertOrUpdate(10L, 1500L)).thenReturn(new UserPoint(10L, 1500L, System.currentTimeMillis()));

        // when
        // 1000포인트 충전
        Long id = 10L;
        Long amount = 1000L;
        UserPoint userPoint = pointService.charge(id, amount);

        // then
        assertEquals(userPoint.point(), 1500L);
        verify(stubHistoryTable, times(1)).insert(eq(id),eq(amount),eq(CHARGE),anyLong());
    }

    @Test
    @DisplayName("id에 해당하는 유저가 amount만큼 포인트를 사용하면 그 액수만큼 보유포인트가 차감된다.")
    void use(){
        /***
         * 기존 1500포인트 보유하고 있는 고객이 500포인트를 사용하면 1000포인트가 된다.
         */
        // given
        UserPointTable stubUserPointTable = mock(UserPointTable.class);
        PointHistoryTable stubHistoryTable = mock(PointHistoryTable.class);
        PointService pointService = new PointService(stubUserPointTable, stubHistoryTable);

        when(stubUserPointTable.selectById(10L)).thenReturn(new UserPoint(10L, 1500L, System.currentTimeMillis()));
        when(stubUserPointTable.insertOrUpdate(10L, 1000L)).thenReturn(new UserPoint(10L, 1000L, System.currentTimeMillis()));

        // when
        // 500포인트 사용
        Long id = 10L;
        Long amount = 500L;
        UserPoint userPoint = pointService.use(id, amount);

        // then
        assertEquals(userPoint.point(), 1000L);
        verify(stubHistoryTable, times(1)).insert(eq(id),eq(amount),eq(USE),anyLong());
    }

    @Test
    @DisplayName("id에 해당하는 유저가 amount만큼 포인트를 사용시, 잔고가 부족할 경우 실패한다.")
    void useFail(){
        /***
         * 기존 1500포인트 보유하고 있는 고객이 2000포인트를 사용시 실패한다.
         */
        // given
        UserPointTable stubUserPointTable = mock(UserPointTable.class);
        PointHistoryTable stubHistoryTable = mock(PointHistoryTable.class);
        PointService pointService = new PointService(stubUserPointTable, stubHistoryTable);

        when(stubUserPointTable.selectById(10L)).thenReturn(new UserPoint(10L, 1500L, System.currentTimeMillis()));

        // when
        // 2000포인트 사용
        Long id = 10L;
        Long amount = 2000L;

        // then
        assertThrows(RuntimeException.class, () -> pointService.use(id, amount));
    }

    @Test
    @DisplayName("동시성 테스트- 동시에 충전요청이 들어와도 포인트가 정확히 적재된다.")
    void concurrentCharge() throws InterruptedException {
        /**
         * 10개 스레드가 동시에 10포인트씩 충전하면 100원이 충전된다.
         */
        // given
        // 초기 0포인트
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        PointService pointService = new PointService(userPointTable, pointHistoryTable);

        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 0L); // 초기 포인트 0 설정

        // when
        // 10개 스레드가 10 포인트씩 동시에 충전
        int threadCount = 10;
        long chargeAmount = 10L;

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Phaser phaser = new Phaser(threadCount);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                phaser.arriveAndAwaitAdvance(); // 모든 스레드 등록될 때까지 대기했다 동시시작
                try{
                    pointService.charge(userId, chargeAmount);
                } finally { // 예외가 나도 무조건 호출되어 doneSignal.await()의 timeout 방지
                    doneSignal.countDown(); // 해당 작업 스레드 종료 알림
                }
            });
        }

        doneSignal.await(); // 모든 작업 스레드가 doneSignal.countDown()을 호출할 때까지 메인 스레드를 대기
        executorService.shutdown();

        // then
        UserPoint result = userPointTable.selectById(userId);
        assertEquals(threadCount * chargeAmount, result.point());
    }

    @Test
    @DisplayName("동시성 테스트- 동시에 포인트를 사용 시 잔고는 정확히 사용한 만큼 줄어든다.")
    void concurrentUse() throws InterruptedException {
        /**
         * 10개 스레드가 동시에 10포인트씩 충전하면 100원이 충전된다.
         */
        // given
        // 초기 10000포인트
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        PointService pointService = new PointService(userPointTable, pointHistoryTable);

        long userId = 1L;
        long startPoint = 10000L;
        userPointTable.insertOrUpdate(userId, startPoint); // 초기 포인트 1000L 설정

        // when
        // 10개 스레드가 500 포인트씩 동시에 충전
        int threadCount = 10;
        long chargeAmount = 500L;

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Phaser phaser = new Phaser(threadCount);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                phaser.arriveAndAwaitAdvance(); // 모든 스레드 등록될 때까지 대기했다 동시시작
                try{
                    pointService.use(userId, chargeAmount);
                } finally { // 예외가 나도 무조건 호출되어 doneSignal.await()의 timeout 방지
                    doneSignal.countDown(); // 해당 작업 스레드 종료 알림
                }
            });
        }

        doneSignal.await(); // 모든 작업 스레드가 doneSignal.countDown()을 호출할 때까지 메인 스레드를 대기
        executorService.shutdown();

        // then
        UserPoint result = userPointTable.selectById(userId);
        assertEquals(startPoint - (threadCount * chargeAmount), result.point());
    }
}