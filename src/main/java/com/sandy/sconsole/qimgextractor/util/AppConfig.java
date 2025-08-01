package com.sandy.sconsole.qimgextractor.util;

import java.io.File ;

import lombok.Data ;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class AppConfig {

    @Value( "${sourceBaseDir}" )
    private File sourceBaseDir ;
    
    @Value( "${repairProjectOnStartup}" )
    private boolean repairProjectOnStartup = false ;
}
