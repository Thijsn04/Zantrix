package com.zantrix.terminology.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "snomed_concepts")
public class Concept {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String preferredTerm;

    @Field(type = FieldType.Keyword)
    private String semanticTag;

    public Concept() {}

    public Concept(String id, String preferredTerm, String semanticTag) {
        this.id = id;
        this.preferredTerm = preferredTerm;
        this.semanticTag = semanticTag;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPreferredTerm() { return preferredTerm; }
    public void setPreferredTerm(String preferredTerm) { this.preferredTerm = preferredTerm; }

    public String getSemanticTag() { return semanticTag; }
    public void setSemanticTag(String semanticTag) { this.semanticTag = semanticTag; }
}
