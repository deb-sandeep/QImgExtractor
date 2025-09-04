package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

@Slf4j
public class QuestionRepo {
    
    private final File persistenceFile ;
    private final ProjectModel projectModel ;
    
    @Getter
    private final List<Question> questionList = new ArrayList<>() ;
    
    QuestionRepo( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
        this.persistenceFile = new File( projectModel.getWorkDir(), "question-info.json" ) ;
        refresh() ;
    }
    
    void refresh() {
        Map<String, Question> qImgClusterMap = new HashMap<>() ;
        Map<String, QuestionImageCluster> lctCtxImgClusterMap = new HashMap<>() ;
        
        log.debug( "Initializing question repository" ) ;
        for( PageImage pageImg : projectModel.getPageImages() ) {
            for( QuestionImage qImg : pageImg.getQImgList() ) {
                String qId = qImg.getQId().toString() ;
                QuestionImageCluster cluster ;
                if( qId.endsWith( "Ctx" ) ) {
                    cluster = lctCtxImgClusterMap.computeIfAbsent( qId, id -> new QuestionImageCluster( qImg.getQId() ) ) ;
                }
                else {
                    cluster = qImgClusterMap.computeIfAbsent( qId, id -> new Question( qImg.getQId() ) ) ;
                }
                cluster.addQImg( qImg ) ;
            }
        }
        
        for( Question q : qImgClusterMap.values() ) {
            if( q.isLCT() ) {
                QuestionImageCluster lctCtxCluster = lctCtxImgClusterMap.get( q.getLCTRoot() + "/Ctx" ) ;
                q.addLCTCtxImgCluster( lctCtxCluster ) ;
            }
        }
        
        questionList.clear() ;
        questionList.addAll( qImgClusterMap.values() ) ;
        Collections.sort( questionList ) ;
        
        syncWithPersistedState() ;
        new SwingWorker<>() {
            protected Void doInBackground() {
                save() ;
                return null ;
            }
        }.execute() ;
    }
    
    public void save() {
        
        try {
            JSONArray jsonArray = new JSONArray();
            for( Question question : questionList ) {
                jsonArray.put( question.getSerializedForm() );
            }
            
            JSONObject json = new JSONObject() ;
            json.put( "projectName", projectModel.getProjectName() ) ;
            json.put( "questions", jsonArray ) ;
            
            try( FileWriter file = new FileWriter( persistenceFile ) ) {
                file.write( json.toString( 2 ) );
                file.flush();
            }
            log.debug( "Questions saved to {}", persistenceFile.getAbsolutePath() ) ;
        }
        catch( Exception e ) {
            log.error( "Error saving questions", e ) ;
        }
    }
    
    private void syncWithPersistedState() {
        
        if( !persistenceFile.exists() ) {
            return;
        }
        
        try {
            TopicRepo topicRepo = QImgExtractor.getBean( TopicRepo.class ) ;
            String content = FileUtils.readFileToString( persistenceFile, "UTF-8" ) ;
            JSONObject json = new JSONObject( content ) ;
            JSONArray questions = json.getJSONArray( "questions" );
            
            for( int i = 0; i < questions.length(); i++ ) {
                JSONObject qJson = questions.getJSONObject( i );
                String qid = qJson.getString( "qid" );
                
                for( Question q : questionList ) {
                    if( q.qID.toString().equals( qid ) ) {
                        if( !qJson.isNull( "answer" ) ) {
                            try {
                                q.setAnswer( qJson.getString( "answer" ) );
                            }
                            catch( Question.InvalidAnswerException e ) {
                                log.error( "Invalid answer found in persisted state.", e ) ;
                            }
                        }
                        
                        if( !qJson.isNull( "topic" ) ) {
                            JSONObject topicJson = qJson.getJSONObject( "topic" );
                            int topicId = topicJson.getInt( "id" ) ;
                            q.setTopic( topicRepo.getTopicById( topicId ) ) ;
                        }
                        break;
                    }
                }
            }
        }
        catch( Exception e ) {
            log.error( "Error synchronizing with persisted state", e );
        }
    }
}
