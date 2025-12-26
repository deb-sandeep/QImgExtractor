package com.sandy.sconsole.qimgextractor.util.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class APIClient {
    
    private static final MediaType DEFAULT_JSON =
            MediaType.parse( "application/json; charset=utf-8" );
    
    private final ObjectMapper mapper;
    private final OkHttpClient client;
    
    public interface APIClientListener {
        void onAPIClientMsg( String msg ) ;
    }
    
    public APIClient() {
        // Jackson mapper (tweak as you like)
        this.mapper = new ObjectMapper();
        
        // Connection pool: up to 10 idle connections, keep-alive 5 minutes
        ConnectionPool pool = new ConnectionPool( 10, 5, TimeUnit.MINUTES );
        
        // OkHttpClient with sane defaults
        this.client = new OkHttpClient.Builder()
                .connectionPool( pool )
                .connectTimeout( Duration.ofSeconds( 10 ) )
                .readTimeout( Duration.ofSeconds( 30 ) )
                .callTimeout( Duration.ofSeconds( 60 ) )
                .build();
    }
    
    public APIResponse get( String url ) throws IOException {
        return get( url, Collections.emptyMap() );
    }
    
    public APIResponse get( String url, Map<String, String> headers ) throws IOException {
        Request.Builder rb = new Request.Builder().url( url );
        addHeaders( rb, headers );
        
        try( Response res = client.newCall( rb.build() ).execute() ) {
            return wrap( res );
        }
    }
    
    public APIResponse post( String url, Object body, APIClientListener listener ) throws IOException {
        return post( url, null, null, body, listener );
    }
    
    public APIResponse post( String url, String body, APIClientListener listener ) throws IOException {
        return post( url, null, null, body, listener );
    }
    
    public APIResponse post( String url,
                             Map<String, String> headers,
                             Map<String, String> body,
                             APIClientListener listener ) throws IOException {
        return post( url, headers, null, body, listener );
    }
    
    public APIResponse post( String url,
                             Map<String, String> headers,
                             Object body,
                             APIClientListener listener ) throws IOException {
        return post( url, headers, null, body, listener );
    }
    
    public APIResponse post( String url,
                             Map<String, String> headers,
                             String contentType,
                             Object body,
                             APIClientListener listener ) throws IOException {
        
        MediaType mt = MediaType.parse( ( contentType == null || contentType.isBlank() ) ?
                DEFAULT_JSON.toString() : contentType );
        
        if( listener != null ) {
            listener.onAPIClientMsg( "  POST " + url ) ;
        }
        
        String bodyString;
        if( body == null ) {
            bodyString = "";
        }
        else if( body instanceof String ) {
            bodyString = ( String )body;
        }
        else {
            bodyString = mapper.writeValueAsString( body );
        }
        
        if( listener != null ) {
            listener.onAPIClientMsg( "  Body length " + bodyString.length() + " bytes" ) ;
        }
        
        RequestBody rb = RequestBody.create( mt, bodyString ) ;
        Request.Builder req = new Request.Builder().url( url ).post( rb );
        addHeaders( req, headers );
        
        log.debug( "POST {} > {}", url, bodyString ) ;
        if( listener != null ) {
            listener.onAPIClientMsg( "  Sending request... " ) ;
        }
        try( Response res = client.newCall( req.build() ).execute() ) {
            log.debug( "< {} {}", res.code(), res.message() ) ;
            APIResponse response = wrap( res ) ;
            if( listener != null ) {
                listener.onAPIClientMsg( "  Response code " + response.code() ) ;
            }
            return response ;
        }
    }

    private static void addHeaders( Request.Builder rb, Map<String, String> headers ) {
        if( headers != null ) {
            headers.forEach( ( k, v ) -> {
                if( k != null && v != null )
                    rb.addHeader( k, v );
            } );
        }
    }
    
    private static String applyPathParams( String url, String[] params ) {
        if( params == null || params.length == 0 )
            return url;
        
        String result = url;
        for( int i = 0; i < params.length; i++ ) {
            String encoded = params[i] == null ? "" : URLEncoder.encode( params[i], StandardCharsets.UTF_8 );
            result = result.replace( "{" + i + "}", encoded );
        }
        return result;
    }
    
    private APIResponse wrap( Response res ) throws IOException {
        ResponseBody b = res.body();
        String bodyStr = ( b != null ) ? b.string() : "";
        String ct = res.header( "Content-Type" );
        return new APIResponse( res.code(), bodyStr.length(), bodyStr, ct, mapper );
    }
}
