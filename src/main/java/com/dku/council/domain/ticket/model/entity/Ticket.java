package com.dku.council.domain.ticket.model.entity;

import com.dku.council.domain.user.model.entity.User;
import com.dku.council.global.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private TicketEvent event;

    private int turn;

    public Ticket(User user, TicketEvent event, int turn) {
        this.user = user;
        this.event = event;
        this.turn = turn;
    }
}
