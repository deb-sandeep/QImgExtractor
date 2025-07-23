package com.sandy;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;

import java.io.File;

public class QImgParsingTest {
    public static void main( String[] args ) {
        String[] ids = {
                "AITS-25-M-FT9.P_SCA_1.png",
                "AITS-25-M-FT9.P_MCA_5.png",
                "AITS-25-M-FT9.P_LCT_1_Ctx(1).png",
                "AITS-25-M-FT9.P_LCT_1_Ctx(2).png",
                "AITS-25-M-FT9.P_LCT_1_1.png",
        } ;
        
        QuestionImage q ;
        for( String id : ids ) {
            System.out.println( id ) ;
            File file = new File( id ) ;
            q = new QuestionImage( file ) ;
            System.out.println( "\t" + q.getLongFileName() + " : " + id.equals( q.getLongFileName() ) ) ;
            
            q = q.nextQuestion() ;
            System.out.println( "\t" + q.getLongFileName() ) ;
        }
    }
}
