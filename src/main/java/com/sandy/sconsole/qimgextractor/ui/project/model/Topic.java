package com.sandy.sconsole.qimgextractor.ui.project.model;

import lombok.Data;
import org.springframework.boot.configurationprocessor.json.JSONObject;

@Data
public class Topic implements Comparable<Topic> {
    
    private int id ;
    private String name ;
    private String syllabusName;
    
    public Topic(){}
    
    public Topic( int id, String name, String syllabusName ) {
        this.id = id;
        this.name = name;
        this.syllabusName = syllabusName;
    }
    
    public JSONObject getSerializedForm() throws Exception {
        JSONObject json = new JSONObject() ;
        json.put( "id", id ) ;
        json.put( "name", name ) ;
        json.put( "syllabusName", syllabusName ) ;
        return json ;
    }
    
    @Override
    public int compareTo( Topic t ) {
        if( syllabusName.compareTo( t.syllabusName ) == 0 ) {
            return id - t.id ;
        }
        return syllabusName.compareTo( t.syllabusName ) ;
    }
}
