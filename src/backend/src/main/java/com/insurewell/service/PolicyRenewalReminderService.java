package com.insurewell.service;

import com.insurewell.dto.PolicyRenewalReminderDTO;
import com.insurewell.model.Policy;
import com.insurewell.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyRenewalReminderService {
  private final PolicyRepository policyRepository;
  private final Clock clock;

  public PolicyRenewalReminderService(PolicyRepository policyRepository) {
    this(policyRepository, Clock.systemDefaultZone());
  }

  PolicyRenewalReminderService(PolicyRepository policyRepository, Clock clock) {
    this.policyRepository = policyRepository;
    this.clock = clock;
  }

  public List<PolicyRenewalReminderDTO> getReminders(String policyholderName) {
    LocalDate today = LocalDate.now(clock);
    LocalDate lastEligibleDate = today.plusDays(30);

    return policyRepository.findByHolderName(policyholderName).stream()
      .filter(policy -> policyholderName.equals(policy.getHolderName()))
      .filter(policy -> "active".equalsIgnoreCase(policy.getStatus()))
      .map(policy -> toReminder(policy, today, lastEligibleDate))
      .filter(reminder -> reminder != null)
      .sorted(Comparator.comparing(PolicyRenewalReminderDTO::getEndDate))
      .collect(Collectors.toList());
  }

  private PolicyRenewalReminderDTO toReminder(Policy policy, LocalDate today, LocalDate lastEligibleDate) {
    try {
      LocalDate endDate = LocalDate.parse(policy.getEndDate());
      if (endDate.isBefore(today) || endDate.isAfter(lastEligibleDate)) {
        return null;
      }
      return PolicyRenewalReminderDTO.builder()
        .policyId(policy.getId())
        .planName(policy.getPlanName())
        .endDate(endDate.toString())
        .daysRemaining(ChronoUnit.DAYS.between(today, endDate))
        .build();
    } catch (DateTimeParseException exception) {
      return null;
    }
  }
}
