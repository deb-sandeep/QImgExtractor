package com.sandy.sconsole.qimgextractor.ui.project.model;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class QuestionRepo {
    
    private final ProjectModel projectModel ;
    private final List<Question> questionList = new ArrayList<>() ;
    
    QuestionRepo( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
        initialize() ;
    }
    
    private void initialize() {
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
        
        questionList.addAll( qImgClusterMap.values() ) ;
        Collections.sort( questionList ) ;
        
        for( Question q : questionList ) {
            q.logContents() ;
        }
    }
}
