package com.zantrix.documentation.internal;

import com.zantrix.documentation.NoteRequest;
import com.zantrix.documentation.NoteSection;
import com.zantrix.documentation.NoteSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentationService {

    private final FhirAccessGateway fhir;
    public DocumentationService(FhirAccessGateway fhir) { this.fhir = fhir; }

    public NoteSummary draft(NoteRequest request) {
        Composition note = new Composition();
        note.setId(UUID.randomUUID().toString());
        note.setStatus(Composition.CompositionStatus.PRELIMINARY);
        note.setType(new CodeableConcept(new Coding(request.typeSystem(), request.typeCode(), request.typeDisplay())));
        note.addCategory(new CodeableConcept(new Coding("http://loinc.org", "34109-9", "Note")));
        note.setSubject(new Reference("Patient/" + request.patientId()));
        note.setEncounter(new Reference("Encounter/" + request.encounterId()));
        note.setDate(new Date());
        note.addAuthor(new Reference("Practitioner/" + request.authorId()));
        note.setTitle(request.title());
        request.sections().forEach(section -> addSection(note, section));
        fhir.create(note, request.patientId());
        return summary(note);
    }

    public NoteSummary update(String id, NoteRequest request) {
        Composition note = fhir.read(Composition.class, id, request.patientId());
        if (note.getStatus() != Composition.CompositionStatus.PRELIMINARY) {
            throw new IllegalArgumentException("A signed note is immutable; create an addendum instead");
        }
        note.setType(new CodeableConcept(new Coding(request.typeSystem(), request.typeCode(), request.typeDisplay())));
        note.setTitle(request.title());
        note.setDate(new Date());
        note.getSection().clear();
        request.sections().forEach(section -> addSection(note, section));
        fhir.update(note, request.patientId());
        return summary(note);
    }

    public NoteSummary sign(String id, String patientId, String signerId) {
        Composition note = fhir.read(Composition.class, id, patientId);
        if (note.getStatus() != Composition.CompositionStatus.PRELIMINARY) {
            throw new IllegalArgumentException("Only a preliminary note can be signed");
        }
        note.setStatus(Composition.CompositionStatus.FINAL);
        note.setDate(new Date());
        note.addAttester().setMode(Composition.CompositionAttestationMode.PROFESSIONAL)
                .setTime(new Date()).setParty(new Reference("Practitioner/" + signerId));
        fhir.update(note, patientId);
        return summary(note);
    }

    public NoteSummary addendum(String id, String patientId, String authorId, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Addendum text is required");
        }
        Composition note = fhir.read(Composition.class, id, patientId);
        if (note.getStatus() != Composition.CompositionStatus.FINAL
                && note.getStatus() != Composition.CompositionStatus.AMENDED) {
            throw new IllegalArgumentException("Only a signed note can receive an addendum");
        }
        note.setStatus(Composition.CompositionStatus.AMENDED);
        note.setDate(new Date());
        note.addAuthor(new Reference("Practitioner/" + authorId));
        addSection(note, new NoteSection("http://loinc.org", "55107-7", "Addendum", text));
        fhir.update(note, patientId);
        return summary(note);
    }

    public List<NoteSummary> list(String patientId) {
        return FhirBundles.resources(fhir.search("Composition", Map.of(
                "patient", List.of(patientId), "_sort", List.of("-date"), "_count", List.of("300")),
                patientId), Composition.class).stream().map(DocumentationService::summary).toList();
    }

    private static void addSection(Composition note, NoteSection input) {
        Composition.SectionComponent section = note.addSection();
        section.setTitle(input.display());
        section.setCode(new CodeableConcept(new Coding(input.codeSystem(), input.code(), input.display())));
        Narrative narrative = new Narrative().setStatus(Narrative.NarrativeStatus.GENERATED);
        narrative.setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>"
                + escape(input.text()) + "</p></div>");
        section.setText(narrative);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static NoteSummary summary(Composition note) {
        return new NoteSummary(note.getIdElement().getIdPart(),
                note.getSubject().getReferenceElement().getIdPart(),
                note.getEncounter().getReferenceElement().getIdPart(), note.getTitle(),
                note.getType().getCodingFirstRep().getDisplay(), note.getStatus().toCode(),
                note.getAuthor().isEmpty() ? null : note.getAuthorFirstRep().getReferenceElement().getIdPart(),
                note.getDate() == null ? null : note.getDate().toInstant(),
                note.getMeta().getVersionId() == null ? 1 : parseVersion(note.getMeta().getVersionId()));
    }

    private static int parseVersion(String value) {
        try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return 1; }
    }
}
