package com.zantrix.terminology.repository;

import com.zantrix.terminology.domain.Concept;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ConceptRepository extends ElasticsearchRepository<Concept, String> {
    List<Concept> findByPreferredTermContainingIgnoreCase(String term);
}
