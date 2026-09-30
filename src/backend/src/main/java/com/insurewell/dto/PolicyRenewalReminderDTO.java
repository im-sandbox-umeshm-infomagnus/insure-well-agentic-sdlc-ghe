package com.insurewell.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRenewalReminderDTO {
  private String policyId;
  private String planName;
  private String endDate;
  private long daysRemaining;
}
