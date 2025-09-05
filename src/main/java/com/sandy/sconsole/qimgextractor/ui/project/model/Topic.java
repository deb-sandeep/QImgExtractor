package com.sandy.sconsole.qimgextractor.ui.project.model;

import lombok.Data;
import org.springframework.boot.configurationprocessor.json.JSONObject;

@Data
public class Topic {
    
    private int id ;
    private String name ;
    private String syllabusName;
    
    public JSONObject getSerializedForm() throws Exception {
        JSONObject json = new JSONObject() ;
        json.put( "id", id ) ;
        json.put( "name", name ) ;
        json.put( "syllabusName", syllabusName ) ;
        return json ;
    }
    
}
