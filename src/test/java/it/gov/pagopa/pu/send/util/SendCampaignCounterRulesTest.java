package it.gov.pagopa.pu.send.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SendCampaignCounterRulesTest {
  @Test
  void checkMutuallyExclusiveDeactivationLogic() {
    CampaignCounterRules.COUNTER_RULES.forEach((counterName, rule) -> {
      boolean hasDeactivatingCounters = !rule.getDeactivatingCounters().isEmpty();
      boolean hasDeactivationConditions = !rule.getDeactivationConditions().isEmpty();

      assertFalse(hasDeactivatingCounters && hasDeactivationConditions);
    });
  }

  @Test
  void checkNoCircularDependencies() {
    for (String startNode : CampaignCounterRules.COUNTER_RULES.keySet()) {
      Set<String> alreadyChecked = new HashSet<>();
      List<String> countersToCheck = new ArrayList<>(CampaignCounterRules.COUNTER_RULES.get(startNode).getDeactivatingCounters());

      while (!countersToCheck.isEmpty()) {
        String currentNode = countersToCheck.removeFirst();
        assertNotEquals(currentNode, startNode);

        if (alreadyChecked.add(currentNode)) {
          CampaignCounterRules.CounterRule rule = CampaignCounterRules.COUNTER_RULES.get(currentNode);
          countersToCheck.addAll(rule.getDeactivatingCounters());
        }
      }
    }
  }
}
