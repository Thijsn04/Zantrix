package com.zantrix.cds.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Versioned open knowledge pack based on the ONC-sponsored consensus list of
 * drug pairs that should always alert in an EHR. Codes are RxNorm ingredients.
 */
final class HighPriorityInteractionKnowledgeBase {

    static final String NAME = "Zantrix Open High-Priority DDI Pack";
    static final String VERSION = "2026.07-Phansalkar15";
    static final String SOURCE = "doi:10.1136/amiajnl-2011-000612";

    private static final Set<String> AMPHETAMINES = set("3288","725","8896","700810","38400","8152","352372","3389","33272","1422","6816");
    private static final Set<String> MAOIS = set("8123","9639","10734","8702","6011");
    private static final Set<String> NARCOTICS = set("10689","4337","6813","3289","6754","787390");
    private static final Set<String> TCAS = set("704","7531","3638","3247","5691","2597","8886","10834");
    private static final Set<String> TRIPTANS = set("37418","135775","88014");
    private static final Set<String> SSRI_SNRI = set("2556","36437","4493","39786","72625","321988","32937","734064","42355","588250","31565");
    private static final Set<String> CITALOPRAM = set("2556");
    private static final Set<String> QT_AGENTS = set("26225","4450","2551","18631","321988","4053","135447","6813","703","82122","139462","5093","9947","4441","21212","21107","2403","49247","2393","3541","233698","596724","9068","7994","662281","8331","10502","1098413");
    private static final Set<String> IRINOTECAN = set("51499");
    private static final Set<String> CYP3A4_INHIBITORS = set("6135","4450","3443","4053","11170","703","21212","85762","460132","343047","195088","2541","121243","358255","28031","31565");
    private static final Set<String> STATINS = set("36567","6472");
    private static final Set<String> ERGOTS = set("4025","6883","3418","4021");
    private static final Set<String> TIZANIDINE = set("57258");
    private static final Set<String> CYP1A2_INHIBITORS = set("2551","703","8754","42355","6926","40575","10594");
    private static final Set<String> RAMELTEON = set("596205");
    private static final Set<String> SPECIFIC_CYP1A2 = set("2551","703","42355","10594");
    private static final Set<String> ATAZANAVIR = set("343047");
    private static final Set<String> PPIS = set("7646","40790","283742","17128","114979");
    private static final Set<String> FEBUXOSTAT = set("73689");
    private static final Set<String> THIOPURINES = set("1256","103");
    private static final Set<String> TRANYLCYPROMINE = set("10734");
    private static final Set<String> PROCARBAZINE = set("8702");
    private static final Set<String> CYP3A4_INDUCERS = set("2002","9384","75207","258326","55672","35617");
    private static final Set<String> PROTEASE_INHIBITORS = set("85762","460132","343047","195088","358262","114289","134527","83395","228656","190548");

    private static final List<Rule> RULES = List.of(
            rule("HPDDI-01", AMPHETAMINES, MAOIS, "Amphetamine-class medicine with an MAO inhibitor may cause hypertensive crisis or serotonin toxicity."),
            rule("HPDDI-02", NARCOTICS, MAOIS, "Selected opioid or antitussive with an MAO inhibitor may cause severe serotonin or central nervous system toxicity."),
            rule("HPDDI-03", TCAS, MAOIS, "Tricyclic antidepressant with an MAO inhibitor may cause hypertensive crisis or serotonin toxicity."),
            rule("HPDDI-04", TRIPTANS, MAOIS, "Selected triptan with an MAO inhibitor may cause severe serotonin toxicity."),
            rule("HPDDI-05", SSRI_SNRI, MAOIS, "Serotonergic antidepressant with an MAO inhibitor may cause life-threatening serotonin syndrome."),
            rule("HPDDI-06", CITALOPRAM, QT_AGENTS, "Concurrent QT-prolonging medicines may cause a life-threatening ventricular arrhythmia."),
            rule("HPDDI-07", IRINOTECAN, CYP3A4_INHIBITORS, "Strong CYP3A4 inhibition may substantially increase irinotecan toxicity."),
            rule("HPDDI-08", STATINS, CYP3A4_INHIBITORS, "CYP3A4 inhibition may substantially increase simvastatin or lovastatin exposure and rhabdomyolysis risk."),
            rule("HPDDI-09", ERGOTS, CYP3A4_INHIBITORS, "CYP3A4 inhibition may cause severe ergot toxicity and ischemia."),
            rule("HPDDI-10", TIZANIDINE, CYP1A2_INHIBITORS, "CYP1A2 inhibition may greatly increase tizanidine exposure, hypotension, and sedation."),
            rule("HPDDI-11", RAMELTEON, SPECIFIC_CYP1A2, "CYP1A2 inhibition may greatly increase ramelteon exposure."),
            rule("HPDDI-12", ATAZANAVIR, PPIS, "Proton pump inhibition may reduce atazanavir exposure and antiviral effectiveness."),
            rule("HPDDI-13", FEBUXOSTAT, THIOPURINES, "Febuxostat may dangerously increase thiopurine exposure and life-threatening myelosuppression."),
            rule("HPDDI-14", TRANYLCYPROMINE, PROCARBAZINE, "Concurrent monoamine oxidase inhibition may cause hypertensive crisis or serotonin toxicity."),
            rule("HPDDI-15", CYP3A4_INDUCERS, PROTEASE_INHIBITORS, "Strong CYP3A4 induction may reduce protease-inhibitor exposure and lead to virologic failure or resistance.")
    );

    List<Interaction> find(String firstCode, String secondCode) {
        List<Interaction> matches = new ArrayList<>();
        for (Rule rule : RULES) {
            if ((rule.first().contains(firstCode) && rule.second().contains(secondCode))
                    || (rule.first().contains(secondCode) && rule.second().contains(firstCode))) {
                matches.add(new Interaction(rule.id(), rule.summary()));
            }
        }
        return List.copyOf(matches);
    }

    private static Rule rule(String id, Set<String> first, Set<String> second, String summary) {
        return new Rule(id, first, second, summary);
    }

    private static Set<String> set(String... values) { return Set.of(values); }

    record Interaction(String ruleId, String summary) { }
    private record Rule(String id, Set<String> first, Set<String> second, String summary) { }
}
