package com.insurewell.service;

import com.insurewell.model.Policy;
import com.insurewell.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyRenewalReminderServiceTest {
  @Mock
  private PolicyRepository policyRepository;

  @Test
  void returnsOnlyCurrentPolicyholdersActivePoliciesWithinInclusiveDateRangeInOrder() {
    PolicyRenewalReminderService service = new PolicyRenewalReminderService(
      policyRepository, Clock.fixed(Instant.parse("2026-09-30T00:00:00Z"), ZoneOffset.UTC));
    when(policyRepository.findByHolderName("Alex Johnson")).thenReturn(List.of(
      policy("POL-LATER", "Alex Johnson", "active", "2026-10-30"),
      policy("POL-TODAY", "Alex Johnson", "active", "2026-09-30"),
      policy("POL-EXPIRED", "Alex Johnson", "active", "2026-09-29"),
      policy("POL-INACTIVE", "Alex Johnson", "inactive", "2026-10-01"),
      policy("POL-TOO-LATE", "Alex Johnson", "active", "2026-10-31"),
      policy("POL-OTHER-HOLDER", "Maria Garcia", "active", "2026-10-01")
    ));

    var reminders = service.getReminders("Alex Johnson");

    assertThat(reminders).extracting("policyId").containsExactly("POL-TODAY", "POL-LATER");
    assertThat(reminders.get(0).getDaysRemaining()).isZero();
    assertThat(reminders.get(1).getDaysRemaining()).isEqualTo(30);
    verify(policyRepository).findByHolderName("Alex Johnson");
  }

  private Policy policy(String id, String holderName, String status, String endDate) {
    return Policy.builder()
      .id(id)
      .holderName(holderName)
      .planName("Plan " + id)
      .status(status)
      .endDate(endDate)
      .build();
  }
}
