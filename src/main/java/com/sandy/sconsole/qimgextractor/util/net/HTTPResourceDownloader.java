package com.sandy.sconsole.qimgextractor.util.net;

import ch.qos.logback.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HTTPResourceDownloader {
    
    private static OkHttpClient client = null ;
    private static HTTPResourceDownloader instance = null ;
    
    public static HTTPResourceDownloader instance() {
        if( instance == null ) {
            instance = new HTTPResourceDownloader() ; 
        }
        return instance ;
    }
    
    private HTTPResourceDownloader() {
        initializeHttpClient() ;
    }
    
    private void initializeHttpClient() {
        
        OkHttpClient.Builder builder = new OkHttpClient.Builder() ;
        
        client = builder.cache( null )
                        .retryOnConnectionFailure( true )
                        .build() ;
    }
    
    public String getResource( String url ) 
        throws Exception {
        return getResource( url, (Map<String, String>)null ) ;
    }
    
    public String getResource( String url, String headerResourceName ) 
        throws Exception {
        
        Map<String, String> headers = loadHeaders( headerResourceName ) ;
        return getResource( url, headers ) ;
    }
    
    public String getResource( String url, String headerResourceName,
                               Map<String, String> cookies ) 
            throws Exception {
        
        Map<String, String> headers = loadHeaders( headerResourceName ) ;
        byte[] contents = getResourceAsBytes( url, headers, cookies ) ;
        return new String( contents ) ;
    }
    
    public byte[] getResourceAsBytes( String url ) 
        throws Exception {
        
        return getResourceAsBytes( url, null, null ) ;
    }
    
    public byte[] getResourceAsBytes( String url, String headerResourceName ) 
        throws Exception {
        
        Map<String, String> headers = loadHeaders( headerResourceName ) ;
        return getResourceAsBytes( url, headers, null ) ;
    }
        
    public String getResource( String url, Map<String, String> headers ) 
        throws Exception {
        
        byte[] contents = getResourceAsBytes( url, headers, null ) ;
        return new String( contents ) ;
    }
    
    private byte[] getResourceAsBytes( String url , 
                                       Map<String, String> headers,
                                       Map<String, String> cookies )
        throws Exception {
        
        Request.Builder builder ;
        builder = new Request.Builder()
                             .url( url ) ;
        
        log.debug( "- Downloading resource from {} >", url ) ;
        if( headers != null && !headers.isEmpty() ) {
            for( String key : headers.keySet() ) {
                String value = headers.get( key ) ;
                builder.addHeader( key, value ) ;
                log.debug( "- Header : {} :: {}", key, value );
            }
        }
        
        if( cookies != null && !cookies.isEmpty() ) {
            String cookieHdrVal = collateCookieHeaderValue( cookies ) ;
            builder.addHeader( "cookie", cookieHdrVal ) ;
            log.debug( "- Header : cookie :: {}", cookieHdrVal );
        }
        
        byte[] responseBody ;
        Request request = builder.build() ;
        
        long startTime = System.currentTimeMillis() ;
        try( Response response = client.newCall( request ).execute() ) {

            long endTime = System.currentTimeMillis() ;

            int responseCode = response.code() ;
            log.debug( "- Response code = {}", responseCode );
            
            int timeTaken = (int)( endTime - startTime ) ;
            log.debug( "- Time taken = {} millis", timeTaken );
            
            if( responseCode == 404 ) {
                throw new HTTPException404() ;
            }
            else if( responseCode == 500 ) {
                throw new HTTPException500() ;
            }

            if( response.body() == null ) {
                responseBody = new byte[0] ;
            }
            else {
                responseBody = response.body().bytes() ;
            }
            
            long contentLength = responseBody.length ;
            log.debug( "- Content length = {} KB", ( int )( contentLength / 1024 ) );
            //log.debug( "Response body = " + new String( responseBody ) );
        }
        finally {
            log.debug( "< " ) ;
        }
        
        return responseBody ;
    }
    
    private String collateCookieHeaderValue( Map<String, String> cookies ) {
        StringBuilder sb = new StringBuilder() ;
        cookies.forEach( (key,value) -> sb.append( key )
          .append( "=" )
          .append( value )
          .append( "; " ) ) ;
        return sb.toString() ;
    }
    
    private Map<String, String> loadHeaders( String headerResourceName ) 
        throws Exception {
        
        InputStream is ;
        BufferedReader reader ;
        Map<String, String> headers = new HashMap<>() ;
        String resPath = "/http_headers/" + headerResourceName ;
        
        is = getClass().getResourceAsStream( resPath ) ;
        if( is == null ) {
            throw new RuntimeException( "Header file not found @ " + resPath ) ;
        }
        
        reader = new BufferedReader( new InputStreamReader( is ) ) ;
        String line = reader.readLine() ;
        while( line != null ) {
            if( StringUtil.isNullOrEmpty( line ) ) {
                if( !line.trim().startsWith( "#" ) ) {
                    String[] parts = line.trim().split( ": " ) ;
                    headers.put( parts[0].trim(), parts[1].trim() ) ;
                }
            }
            line = reader.readLine() ;
        }
        
        return headers ;
    }
}
