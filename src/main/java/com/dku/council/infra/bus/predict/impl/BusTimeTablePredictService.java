package com.dku.council.infra.bus.predict.impl;

import com.dku.council.domain.bus.model.BusStation;
import com.dku.council.infra.bus.predict.BusArrivalPredictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BusTimeTablePredictService implements BusArrivalPredictService {

    public static final long FIRST_TIME_HOUR_OFFSET = 1;
    private final Map<String, TimeTable> busTimeTables = new HashMap<>();
    private final TimeTableParser timeTableParser = new TimeTableParser();

    /**
     * 시간표를 기준으로 남은 시간을 예측합니다.
     * 예측할 수 없거나 버스가 없는 경우 null이 반환됩니다.
     * 버스가 있는 경우는 첫차-{@value FIRST_TIME_HOUR_OFFSET}시간 ~ 막차까지로 간주합니다.
     *
     * @param busNo 버스 번호
     * @param now   현재 시각
     * @return 예측된 남은 시간.
     */
    @Nullable
    public Duration remainingNextBusArrival(String busNo, BusStation station, LocalTime now) {
        TimeTable table = busTimeTables.get(busNo);
        String stationDirName = station.name().replaceAll("_", "").toLowerCase();

        if (table == null) {
            table = timeTableParser.parse(String.format("/bustable/%s/%s.table", stationDirName, busNo));
            busTimeTables.put(busNo, table);
        }

        if (isOutbound(table, now)) {
            return null;
        }

        return table.remainingNextBusArrival(now);
    }

    private static boolean isOutbound(TimeTable table, LocalTime now) {
        LocalTime first = table.getFirstTime();
        LocalTime last = table.getLastTime();

        if (Duration.between(first, last).abs().toHours() > 0) {
            first = first.minusHours(FIRST_TIME_HOUR_OFFSET);
        }

        if (first.isBefore(last)) {
            return now.isBefore(first) || now.isAfter(last);
        } else {
            return now.isAfter(last) && now.isBefore(first);
        }
    }
}
