package com.zantrix.cds.internal;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HighPriorityInteractionKnowledgeBaseTest {
    private final HighPriorityInteractionKnowledgeBase pack = new HighPriorityInteractionKnowledgeBase();

    @Test
    void coversEveryPublishedHighPriorityRuleAndIsSymmetric() {
        List<String[]> examples = List.of(
                pair("3288", "8123"), pair("10689", "9639"), pair("704", "10734"),
                pair("37418", "8702"), pair("2556", "6011"), pair("2556", "26225"),
                pair("51499", "6135"), pair("36567", "3443"), pair("4025", "4053"),
                pair("57258", "2551"), pair("596205", "703"), pair("343047", "7646"),
                pair("73689", "1256"), pair("10734", "8702"), pair("2002", "85762"));
        for (int index = 0; index < examples.size(); index++) {
            String expected = "HPDDI-%02d".formatted(index + 1);
            String[] values = examples.get(index);
            assertThat(pack.find(values[0], values[1])).extracting("ruleId").contains(expected);
            assertThat(pack.find(values[1], values[0])).extracting("ruleId").contains(expected);
        }
    }

    @Test
    void unknownPairDoesNotCreateAFalseAlert() {
        assertThat(pack.find("not-a-code", "also-not-a-code")).isEmpty();
    }

    private static String[] pair(String first, String second) { return new String[]{first, second}; }
}
