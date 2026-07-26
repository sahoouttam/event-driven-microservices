package com.event.driven.fulfillment.service.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.event.driven.common.service.entity.BaseEntity;
import com.event.driven.fulfillment.service.enums.FulfillmentStatus;

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
@Table(name = "fulfillments")
public class Fulfillment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private Long addressId;

    private String shippingStreet;

    private String shippingCity;
    
    private String shippingState;

    @Enumerated(EnumType.STRING)
    private FulfillmentStatus fulfillmentStatus;

    private String trackingNumber;

    private String carrier;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "fulfillment", cascade = CascadeType.ALL)
    private List<FulfillmentItem> fulfillmentItems;
}
