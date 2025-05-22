package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final Map<Long, Object> locks = new ConcurrentHashMap<>();

    public UserPoint getUserPoint(Long id){
        return userPointTable.selectById(id);
    }

    public List<PointHistory> getHistory(Long id){
        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint charge(Long id, Long amount){
        Object lock = locks.computeIfAbsent(id, k -> new Object());
        synchronized (lock){// todo: 실제 db 사용 시 트랜젝션 제어 필요, 분산환경 시 락 사용불가
            Long curPoint = userPointTable.selectById(id).point();
            UserPoint res =  userPointTable.insertOrUpdate(id, curPoint + amount);
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
            return res;
        }
    }

    public UserPoint use(Long id, Long amount){
        Object lock = locks.computeIfAbsent(id, k -> new Object());
        synchronized (lock){ // todo: 실제 db 사용 시 트랜젝션 제어 필요, 분산환경 시 락 사용불가
            Long curPoint = userPointTable.selectById(id).point();

            Long leftPoint = curPoint - amount;
            if(leftPoint < 0 ) throw new RuntimeException();

            UserPoint res = userPointTable.insertOrUpdate(id, leftPoint);
            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
            return res;
        }
    }
}
