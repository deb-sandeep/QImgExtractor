package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Who produced the AI topic recommendations of a question, as recorded in the
 * {@code classifiedBy} object of an {@code ai-topic-map.json} entry. Entries
 * written before provenance was recorded have no {@code classifiedBy}.
 */
@Getter
@RequiredArgsConstructor
public class AIClassifiedBy {

    private final String actor ; // "AI" or "User"
    private final String agent ; // "gemma", "openrouter" or null for User
    private final String model ; // Null for User
}
