package com.dku.council.domain.ticket.model.dto;

import com.dku.council.domain.ticket.model.entity.TicketEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TicketEventDto {
    @Schema(description = "티켓 이벤트 아이디")
    private final Long id;

    @Schema(description = "티켓 이벤트 이름")
    private final String name;

    @Schema(description = "시작 시각")
    private final LocalDateTime from;

    @Schema(description = "종료 시각")
    private final LocalDateTime to;

    @Schema(description = "남은 티켓 수")
    private final int available;

    public TicketEventDto(Long id, String name, LocalDateTime from, LocalDateTime to, int available) {
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
        this.available = available;
    }

    public TicketEventDto(TicketEvent e) {
        this.id = e.getId();
        this.name = e.getName();
        this.from = e.getBegin();
        this.to = e.getEnd();
        this.available = e.getAvailable();
    }
}
