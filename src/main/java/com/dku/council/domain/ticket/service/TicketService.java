package com.dku.council.domain.ticket.service;

import com.dku.council.domain.ticket.exception.AlreadyIssuedTicketException;
import com.dku.council.domain.ticket.exception.InvalidTicketPeriodException;
import com.dku.council.domain.ticket.exception.NoTicketException;
import com.dku.council.domain.ticket.model.dto.TicketEventDto;
import com.dku.council.domain.ticket.model.dto.response.ResponseTicketDto;
import com.dku.council.domain.ticket.model.entity.Ticket;
import com.dku.council.domain.ticket.repository.TicketMemoryRepository;
import com.dku.council.domain.ticket.repository.TicketRepository;
import com.dku.council.domain.user.exception.NotAttendingException;
import com.dku.council.domain.user.model.AcademicStatus;
import com.dku.council.domain.user.model.UserInfo;
import com.dku.council.domain.user.service.UserInfoCacheService;
import com.dku.council.global.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository persistenceRepository;
    private final TicketMemoryRepository memoryRepository;
    private final TicketEventService ticketEventService;
    private final UserInfoCacheService userInfoCacheService;

    @Transactional(readOnly = true)
    public ResponseTicketDto myTicket(Long userId, Long ticketEventId) {
        int turn = memoryRepository.getMyTicket(userId, ticketEventId);
        if (turn == -1) {
            Ticket ticket = persistenceRepository.findByUserIdAndEventId(userId, ticketEventId)
                    .orElseThrow(NoTicketException::new);
            turn = memoryRepository.saveMyTicket(userId, ticketEventId, ticket.getTurn());
        }
        return new ResponseTicketDto(turn);
    }

    /**
     * 티켓 발급
     *
     * @param userId        사용자 ID. 실제 사용자인지 확인하지 않습니다.
     * @param ticketEventId 티켓 이벤트 ID
     * @return 발급된 티켓 정보
     */
    public ResponseTicketDto enroll(Long userId, Long ticketEventId, Instant now) {
        TicketEventDto event = ticketEventService.findEventById(ticketEventId);
        Instant eventFrom = DateUtil.toInstant(event.getFrom());
        Instant eventTo = DateUtil.toInstant(event.getTo());

        if (now.isBefore(eventFrom) || now.isAfter(eventTo)) {
            throw new InvalidTicketPeriodException();
        }

        UserInfo userInfo = userInfoCacheService.getUserInfo(userId);
        if (!userInfo.getAcademicStatus().equals(AcademicStatus.ATTENDING.getLabel())) {
            throw new NotAttendingException();
        }

        int turn = memoryRepository.enroll(userId, ticketEventId);
        if (turn == -1) {
            throw new AlreadyIssuedTicketException();
        }

        return new ResponseTicketDto(turn);
    }
}