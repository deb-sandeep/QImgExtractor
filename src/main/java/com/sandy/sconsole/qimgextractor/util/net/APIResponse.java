package com.sandy.sconsole.qimgextractor.util.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.Objects;

public final class APIResponse {
    
    private final int code;
    private final long contentLength;
    private final String body;
    private final String contentType;
    
    private final ObjectMapper mapper;
    private volatile JsonNode cachedJsonNode;      // lazy
    private volatile JSONObject cachedJsonObject;  // lazy
    
    APIResponse( int code, long contentLength, String body, String contentType, ObjectMapper mapper ) {
        this.code = code;
        this.contentLength = contentLength;
        this.body = ( body == null ) ? "" : body;
        this.contentType = contentType;
        this.mapper = Objects.requireNonNull( mapper );
    }
    
    public int code() {
        return code;
    }
    
    public long contentLength() {
        return contentLength;
    }
    
    public String body() {
        return body;
    }
    
    public String contentType() {
        return contentType;
    }
    
    public JsonNode json() {
        if( !isJson() ) {
            throw new IllegalStateException( "Response is not JSON (Content-Type=" + contentType + ")" );
        }
        if( cachedJsonNode == null ) {
            synchronized( this ) {
                if( cachedJsonNode == null ) {
                    try {
                        cachedJsonNode = mapper.readTree( body );
                    }
                    catch( JsonProcessingException e ) {
                        throw new IllegalStateException( "Failed to parse JSON body", e );
                    }
                }
            }
        }
        return cachedJsonNode;
    }
    
    public boolean isJson() {
        if( contentType != null && contentType.toLowerCase().contains( "application/json" ) )
            return true;
        String trimmed = body.trim();
        return ( trimmed.startsWith( "{" ) && trimmed.endsWith( "}" ) ) || ( trimmed.startsWith( "[" ) && trimmed.endsWith( "]" ) );
    }
    
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer( "APIResponse{ \n" );
        sb.append( "  code=" ).append( code );
        sb.append( ",\n  contentLength=" ).append( contentLength );
        sb.append( ",\n  body='" ).append( body ).append( '\'' );
        sb.append( ",\n  contentType='" ).append( contentType ).append( '\'' );
        sb.append( "\n}\n" );
        return sb.toString();
    }
}
