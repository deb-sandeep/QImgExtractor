package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A single AI topic recommendation for a question, carrying the resolved
 * {@link Topic} and the confidence level (0-100) reported in
 * {@code <project>/.workspace/ai-topic-map.json}.
 */
@Getter
@RequiredArgsConstructor
public class AITopicSuggestion {

    private final Topic topic ;
    private final int confidenceLevel ;
}
