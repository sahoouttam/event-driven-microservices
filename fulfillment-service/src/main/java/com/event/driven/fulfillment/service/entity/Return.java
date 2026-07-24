package com.event.driven.fulfillment.service.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.event.driven.common.service.entity.BaseEntity;
import com.event.driven.fulfillment.service.enums.ReturnStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "returns")
public class Return extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long customerId;

    private Long fulfillmentId;

    @Enumerated(EnumType.STRING)
    private ReturnStatus returnStatus;

    private LocalDateTime receivedAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL)
    private List<ReturnItem> returnItems;
}
