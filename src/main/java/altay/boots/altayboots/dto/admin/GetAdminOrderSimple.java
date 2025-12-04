package altay.boots.altayboots.dto.admin;

import altay.boots.altayboots.dto.status.PaidStatus;

import java.time.LocalDateTime;

public record GetAdminOrderSimple(
        Integer orderId,
        String userName,
        LocalDateTime orderStartDate,
        PaidStatus paidStatus
) {
}
