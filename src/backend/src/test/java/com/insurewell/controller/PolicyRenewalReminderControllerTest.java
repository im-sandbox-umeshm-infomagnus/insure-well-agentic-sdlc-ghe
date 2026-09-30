package com.insurewell.controller;

import com.insurewell.repository.PolicyRepository;
import com.insurewell.service.PolicyRenewalReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
class PolicyRenewalReminderControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private PolicyRepository policyRepository;

  @MockBean
  private PolicyRenewalReminderService renewalReminderService;

  @Test
  void scopesRenewalLookupToAuthenticatedPrincipal() throws Exception {
    when(renewalReminderService.getReminders("Alex Johnson")).thenReturn(List.of());

    mockMvc.perform(get("/api/policies/renewals").principal(() -> "Alex Johnson"))
      .andExpect(status().isOk())
      .andExpect(content().json("[]"));

    verify(renewalReminderService).getReminders("Alex Johnson");
  }

  @Test
  void rejectsRequestsWithoutAnAuthenticatedPrincipal() throws Exception {
    mockMvc.perform(get("/api/policies/renewals"))
      .andExpect(status().isUnauthorized());
  }
}
