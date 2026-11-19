package it.gov.pagopa.pu.send.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CampaignCounterRulesTest {
  @Test
  void checkCustomActivationCallbacksBothConfigured() {
    CampaignCounterRules.COUNTER_RULES.forEach((counterName, rule) ->
      assertEquals(rule.getActivationPredicate() != null, rule.getActivationExpressionSupplier() != null)
    );
  }

  @Test
  void checkMutuallyExclusiveActivationLogic() {
    CampaignCounterRules.COUNTER_RULES.forEach((counterName, rule) -> {
      boolean hasActivationConditions = !rule.getActivationConditions().isEmpty();
      boolean hasActivationCallbacks = rule.getActivationPredicate() != null || rule.getActivationExpressionSupplier() != null;

      assertFalse(hasActivationConditions && hasActivationCallbacks);
    });
  }

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
