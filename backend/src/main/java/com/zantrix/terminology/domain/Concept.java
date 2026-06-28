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

    @Field(type = FieldType.Keyword)
    private String codeSystem;

    @Field(type = FieldType.Keyword)
    private String code;

    public Concept() {}

    public Concept(String id, String preferredTerm, String semanticTag, String codeSystem, String code) {
        this.id = id;
        this.preferredTerm = preferredTerm;
        this.semanticTag = semanticTag;
        this.codeSystem = codeSystem;
        this.code = code;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPreferredTerm() { return preferredTerm; }
    public void setPreferredTerm(String preferredTerm) { this.preferredTerm = preferredTerm; }

    public String getSemanticTag() { return semanticTag; }
    public void setSemanticTag(String semanticTag) { this.semanticTag = semanticTag; }

    public String getCodeSystem() { return codeSystem; }
    public void setCodeSystem(String codeSystem) { this.codeSystem = codeSystem; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
