package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.ParserUtil;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.PageImageState;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectContext;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.confirmDestructiveAction;
import static com.sandy.sconsole.qimgextractor.util.AppUtil.showErrorMsg;

@Component
@Slf4j
public class ProjectModel {

    private final AppConfig appConfig ; // Injected
    
    @Getter private File baseDir ;
    @Getter private File pagesDir ;
    @Getter private File workDir ;
    @Getter private File extractedImgDir ;
    @Getter private String projectName ;
    @Getter private ProjectContext context ;
    @Getter private ProjectState state ;
    @Getter private QuestionRepo questionRepo ;
    @Getter private final List<PageImage> pageImages = new ArrayList<>() ;
    
    private final List<ProjectModelListener> listeners = new ArrayList<>() ;
    
    public ProjectModel( AppConfig appConfig ) {
        this.appConfig = appConfig ;
    }
    
    public void initialize( File projectDir ) {
        log.info( "  Initializing project model." ) ;
        
        this.baseDir = projectDir ;
        this.pagesDir = new File( baseDir, "pages" ) ;
        this.workDir = new File( projectDir, ".workspace" ) ;
        this.extractedImgDir = new File( projectDir, "question-images" ) ;
        this.context = new ProjectContext( this ) ;
        this.state = new ProjectState( this ) ;
        this.pageImages.clear() ;
        this.listeners.clear() ;
        this.projectName = projectDir.getName() ;
        
        if( !workDir.exists() ) {
            if( workDir.mkdirs() ) {
                log.info( "  Created workspace directory." ) ;
            }
        }
        
        if( !extractedImgDir.exists() ) {
            if( extractedImgDir.mkdirs() ) {
                log.info( "  Created extracted images directory." ) ;
            }
        }
        
        log.info( "    Project dir : {}", baseDir.getAbsolutePath() ) ;
        log.info( "    Pages dir   : <project-dir>/{}", pagesDir.getName() ) ;
        log.info( "    Work dir    : <project-dir>/{}", workDir.getName() ) ;
        log.info( "    Images dir  : <project-dir>/{}", extractedImgDir.getName() ) ;
        log.info( "    Loading pages...." ) ;
        
        // Every step below that would drop metadata or delete files asks the
        // user first. Declining a step throws ProjectLoadAbortedException
        // before that step writes anything; overwritten metadata files are
        // kept as .bak copies.
        loadPageImages() ;
        confirmDroppingInvalidRegions() ;
        if( appConfig.isRepairProjectOnStartup() ) {
            log.info( "    Repairing project artefacts...." ) ;
            repairProjectArtefacts() ;
        }

        log.info( "    Initializing question repo...." ) ;
        this.questionRepo = new QuestionRepo( this ) ;
        confirmDroppingOrphanedQuestions() ;
        this.questionRepo.refresh() ;
    }

    // Regions that could not be loaded (image missing, unparseable name) or
    // regions files that could not be read at all are dropped when the page
    // metadata is next saved.
    private void confirmDroppingInvalidRegions() {

        List<String> details = new ArrayList<>() ;
        List<PageImage> affectedPages = new ArrayList<>() ;
        for( PageImage pageImg : pageImages ) {
            if( pageImg.getMetadataReadError() != null ) {
                details.add( pageImg.getRegionsFileName() + " : unreadable (" +
                             pageImg.getMetadataReadError() + ")" ) ;
                affectedPages.add( pageImg ) ;
            }
            else if( !pageImg.getRejectedRegions().isEmpty() ) {
                for( String rejected : pageImg.getRejectedRegions() ) {
                    details.add( pageImg.getRegionsFileName() + " : " + rejected ) ;
                }
                affectedPages.add( pageImg ) ;
            }
        }
        if( details.isEmpty() ) {
            return ;
        }

        boolean proceed = confirmDestructiveAction( "Invalid Region Metadata",
                details.size() + " question region(s) could not be loaded and will be " +
                "removed from the page metadata.\n" +
                "A .bak copy of each affected regions file will be kept.",
                details ) ;
        if( !proceed ) {
            throw new ProjectLoadAbortedException( details.size() + " invalid question region(s)" ) ;
        }

        for( PageImage pageImg : affectedPages ) {
            try {
                pageImg.backupAndSaveQuestionImgMetadata() ;
            }
            catch( IOException e ) {
                throw new ProjectLoadAbortedException( "Could not back up " +
                        pageImg.getRegionsFileName() + " : " + e.getMessage() ) ;
            }
        }
    }

    private void confirmDroppingOrphanedQuestions() {

        List<String> orphans = questionRepo.findPersistedQIdsWithoutImages() ;
        if( orphans.isEmpty() ) {
            return ;
        }

        boolean proceed = confirmDestructiveAction( "Questions Without Images",
                orphans.size() + " question(s) in question-info.json have no question " +
                "image in this project.\n" +
                "Their answers, topics and sync information will be removed.\n" +
                "A .bak copy of question-info.json will be kept.",
                orphans ) ;
        if( !proceed ) {
            throw new ProjectLoadAbortedException( orphans.size() + " question(s) without images" ) ;
        }

        try {
            questionRepo.backupPersistenceFile() ;
        }
        catch( IOException e ) {
            throw new ProjectLoadAbortedException( "Could not back up question-info.json : " + e.getMessage() ) ;
        }
    }
    
    // Assumption: Once a project has been loaded, no new pages can be added
    // to the project. The page images existing in the pages folder form the
    // domain of pages on which the project will operate.
    private void loadPageImages() {
        
        File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        Map<String, PageImageState> pageStateMap = loadPageState() ;
        
        assert files != null ;
        
        QuestionImage lastSavedQImg = null ;
        int maxLctSequence = 0 ;

        for( File file : files ) {
            PageImage pageImg = new PageImage( this, file ) ;
            PageImageState state ;
            if( pageStateMap.containsKey( file.getName() ) ) {
                state = pageStateMap.get( file.getName() ) ;
            }
            else {
                state = new PageImageState( file.getName() ) ;
            }
            pageImg.setState( state ) ;
            pageImages.add( pageImg );

            for( QuestionImage qImg : pageImg.getQImgList() ) {
                if( qImg.getQId().isLCT() ) {
                    maxLctSequence = Math.max( maxLctSequence, qImg.getQId().getLctSequence() ) ;
                }
                if( lastSavedQImg == null ||
                    qImg.getQId().compareTo( lastSavedQImg.getQId() ) > 0 ) {
                    lastSavedQImg = qImg ;
                }
            }
        }
        context.setLastSavedImage( lastSavedQImg ) ;
        context.setLastLCTSequence( Math.max( context.getLastLCTSequence(), maxLctSequence ) ) ;
        Collections.sort( pageImages ) ;
    }
    
    // Key is the file name
    private Map<String, PageImageState> loadPageState() {
        
        File file = new File( this.getWorkDir(), "page-states.json" ) ;
        
        Map<String, PageImageState> stateMap = new HashMap<>();
        if( file.exists() ) {
            try {
                ObjectMapper mapper = new ObjectMapper() ;
                PageImageState[] states = mapper.readValue( file, PageImageState[].class );
                for( PageImageState state : states ) {
                    stateMap.put( state.getFileName(), state ) ;
                }
            }
            catch( IOException e ) {
                log.error( "Error loading page states", e );
            }
        }
        return stateMap;
    }
    
    public void savePageState() {
        
        if( getContext().isPauseSavePageState() ) {
            return ;
        }
        
        File file = new File( this.getWorkDir(), "page-states.json" );
        List<PageImageState> states = new ArrayList<>();
        
        for( PageImage pageImage : pageImages ) {
            states.add( pageImage.getState() );
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable( SerializationFeature.INDENT_OUTPUT );
            mapper.writeValue( file, states );
        }
        catch( IOException e ) {
            log.error( "Error saving page states", e );
        }
    }
    
    // This function is driven by the configuration 'repairProjectOnStartup'.
    // If set to true, the extracted image folder will be cleaned and synced
    // with the extracted images metadata. This might cause deletion of images
    // in the question-images folder which are not associated with sub image
    // metadata for a page.
    private void repairProjectArtefacts() {
        
        File[] files = extractedImgDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        if( files == null || files.length == 0 ) {
            return ;
        }
        
        ArrayList<File> toDelete = new ArrayList<>( List.of( files ) ) ;
        for( PageImage pageImg : pageImages ) {
            List<File> subImgFiles = pageImg.getQuestionImgFiles() ;
            for( File subImgFile : subImgFiles ) {
                toDelete.remove( subImgFile ) ;
            }
        }

        if( !toDelete.isEmpty() ) {
            List<String> names = toDelete.stream().map( File::getName ).sorted().toList() ;
            boolean proceed = confirmDestructiveAction( "Delete Extraneous Images",
                    toDelete.size() + " file(s) in question-images have no matching " +
                    "region metadata and will be permanently deleted.",
                    names ) ;
            if( !proceed ) {
                throw new ProjectLoadAbortedException( toDelete.size() + " extraneous image file(s)" ) ;
            }

            for( File file : toDelete ) {
                if( file.delete() ) {
                    log.warn( "      Deleted extraneous image file: <img-dir>/{}", file.getName() ) ;
                }
            }
        }
    }
    
    public void addListener( ProjectModelListener listener ) {
        listeners.add( listener ) ;
    }
    
    public void removeListener( ImgExtractorPanel panel ) {
        listeners.remove( panel ) ;
    }
    
    public void notifyListenersNewQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) {
        questionRepo.refresh() ;
        listeners.forEach( l -> l.newQuestionImgAdded( pageImage, qImg ) ) ;
    }
    
    private void notifyListenersTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) {
        questionRepo.refresh() ;
        listeners.forEach( l -> l.questionTagNameChanged( qImg, oldTagName, newTagName ) ) ;
    }
    
    private void notifyListenersQuestionImgDeleted( QuestionImage qImg ) {
        questionRepo.refresh() ;
        notifyListenersQuestionImgDeletedNoRefresh( qImg ) ;
    }

    private void notifyListenersQuestionImgDeletedNoRefresh( QuestionImage qImg ) {
        listeners.forEach( l -> l.questionImgDeleted( qImg ) ) ;
    }
    
    public void notifyListenersPartSelectionModeUpdated( boolean value ) {
        listeners.forEach( l -> l.partSelectionModeUpdated( value ) ) ;
    }

    public void questionImgTagNameChanged( QuestionImage qImg, String newTagName ) {
        try {
            String oldTagName = qImg.getImgRegionMetadata().getTag() ;
            
            // Update qImg with the new tag name
            qImg.setNewTagName( newTagName ) ;
            qImg.getImgRegionMetadata().setTag( qImg.getShortFileNameWithoutExtension() ) ;
            qImg.getPageImg().saveQuestionImgMetadata() ;
            
            // Change the file name
            File oldFile = qImg.getImgFile() ;
            File newFile = new File( oldFile.getParentFile(), qImg.getLongFileName() ) ;
            FileUtils.moveFile( oldFile, newFile ) ;
            
            // Set the new file in the question image instance
            qImg.setImgFile( newFile ) ;
            
            // Notify the listeners
            notifyListenersTagNameChanged( qImg, oldTagName, newTagName ) ;
        }
        catch( IOException e ) {
            log.error( "Error renaming question image file.", e ) ;
            showErrorMsg( "Failed to rename question image file. Please try again.", e ) ;
        }
    }
    
    // Changes the subject code of the given question images, renaming their
    // files and region metadata. Answers are carried over to the renamed
    // questions, topics are not since they are subject specific.
    // Nothing is changed if any affected question is synced to a server or
    // if a renamed file would clash with an existing question image.
    public void changeSubject( List<QuestionImage> qImgs, String subjectCode ) {

        ParserUtil.validateSubjectCode( subjectCode ) ;

        Set<QuestionImage> targets = Collections.newSetFromMap( new IdentityHashMap<>() ) ;
        qImgs.stream()
             .filter( qImg -> !qImg.getSubjectCode().equals( subjectCode ) )
             .forEach( targets::add ) ;
        if( targets.isEmpty() ) {
            return ;
        }

        List<String> synced = questionRepo.getQuestionList().stream()
                .filter( Question::isSyncedToAnyServer )
                .filter( q -> q.getQImgList().stream().anyMatch( targets::contains ) )
                .map( q -> q.getQID().toString() )
                .toList() ;
        if( !synced.isEmpty() ) {
            throw new IllegalStateException( "Synced questions can't change subject: " +
                                             String.join( ", ", synced ) ) ;
        }

        // Compute the new file names upfront and check for clashes. A clash
        // can be with an existing file or between two renamed images, e.g.
        // P_SCA_1 and C_SCA_1 both becoming M_SCA_1.
        Map<QuestionImage, QuestionImage> renamedMap = new IdentityHashMap<>() ;
        Set<String> newFileNames = new HashSet<>() ;
        List<String> clashes = new ArrayList<>() ;
        for( QuestionImage qImg : targets ) {
            QuestionImage renamed = qImg.getClone() ;
            renamed.setSubjectCode( subjectCode ) ;
            String newFileName = renamed.getLongFileName() ;
            if( !newFileNames.add( newFileName ) ||
                new File( extractedImgDir, newFileName ).exists() ) {
                clashes.add( renamed.getShortFileNameWithoutExtension() ) ;
            }
            renamedMap.put( qImg, renamed ) ;
        }
        if( !clashes.isEmpty() ) {
            throw new IllegalStateException( "Question images already exist for: " +
                                             String.join( ", ", clashes ) ) ;
        }

        // Key is the new QID
        Map<String, String> answers = new HashMap<>() ;
        for( Question q : questionRepo.getQuestionList() ) {
            if( q.getAnswer() == null ) continue ;
            for( QuestionImage qImg : q.getOwnQImgList() ) {
                if( targets.contains( qImg ) ) {
                    answers.putIfAbsent( renamedMap.get( qImg ).getQId().toString(), q.getAnswer() ) ;
                }
            }
        }

        Map<QuestionImage, String> oldTagNames = new IdentityHashMap<>() ;
        Set<PageImage> modifiedPages = new LinkedHashSet<>() ;
        try {
            for( QuestionImage qImg : targets ) {
                File oldFile = qImg.getImgFile() ;
                File newFile = new File( oldFile.getParentFile(), renamedMap.get( qImg ).getLongFileName() ) ;
                FileUtils.moveFile( oldFile, newFile ) ;

                oldTagNames.put( qImg, qImg.getImgRegionMetadata().getTag() ) ;
                qImg.setSubjectCode( subjectCode ) ;
                qImg.getImgRegionMetadata().setTag( qImg.getShortFileNameWithoutExtension() ) ;
                qImg.setImgFile( newFile ) ;
                modifiedPages.add( qImg.getPageImg() ) ;
            }
        }
        catch( IOException e ) {
            log.error( "Error renaming question image file.", e ) ;
            showErrorMsg( "Failed to rename question image file. " +
                          oldTagNames.size() + " of " + targets.size() + " images were renamed.", e ) ;
        }
        finally {
            modifiedPages.forEach( PageImage::saveQuestionImgMetadata ) ;
        }

        questionRepo.refresh() ;
        boolean answersCarried = false ;
        for( Question q : questionRepo.getQuestionList() ) {
            String answer = answers.get( q.getQID().toString() ) ;
            if( answer != null && q.getAnswer() == null ) {
                try {
                    q.setAnswer( answer ) ;
                    answersCarried = true ;
                }
                catch( Question.InvalidAnswerException e ) {
                    log.error( "Could not carry over answer for {}", q.getQRef(), e ) ;
                }
            }
        }
        if( answersCarried ) {
            questionRepo.saveInBackground() ;
        }

        oldTagNames.forEach( ( qImg, oldTagName ) -> listeners.forEach(
                l -> l.questionTagNameChanged( qImg, oldTagName, qImg.getShortFileNameWithoutExtension() ) ) ) ;

        log.info( "Changed subject of {} question images to {}", oldTagNames.size(), subjectCode ) ;
    }

    public void questionImgDeleted( QuestionImage qImg ) {
        // Delete it from the model
        qImg.getPageImg().deleteQuestionImg( qImg ) ;
        
        // Notify the listeners - the tree model and the image canvas
        notifyListenersQuestionImgDeleted( qImg ) ;
        
        // Physically delete the file.
        File imgFile = qImg.getImgFile() ;
        if( imgFile.delete() ) {
            log.info( "Deleted question image file: <img-dir>/{}", imgFile.getName() ) ;
        }
        else {
            log.warn( "Failed to delete question image file: <img-dir>/{}", imgFile.getName() ) ;
        }
    }

    // Deletes all the images (including parts) of the given question and
    // removes its references from the persisted state. The LCT context images
    // are deleted only if this is the last question in its LCT group.
    // Questions synced to either the dev or the prod server can't be deleted.
    public void deleteQuestion( Question question ) {

        if( question.isSyncedToAnyServer() ) {
            throw new IllegalStateException( "Question " + question.getQRef() +
                                             " is synced to server; cannot delete" ) ;
        }

        List<QuestionImage> toDelete = new ArrayList<>( question.getOwnQImgList() ) ;

        boolean deleteLctCtx = false ;
        QuestionImageCluster lctCtxCluster = question.getLctCtxImgCluster() ;
        if( question.isLCT() && lctCtxCluster != null ) {
            // Compare by QID and not identity - the question instance might be
            // stale if the repo has been refreshed since (e.g. bulk deletes).
            String qId = question.getQID().toString() ;
            String lctRoot = question.getLCTRoot() ;
            deleteLctCtx = questionRepo.getQuestionList().stream()
                    .noneMatch( q -> !q.getQID().toString().equals( qId ) &&
                                     lctRoot.equals( q.getLCTRoot() ) ) ;
            if( deleteLctCtx ) {
                toDelete.addAll( lctCtxCluster.getQImgList() ) ;
            }
        }

        // Remove from the page models. This persists the page region metadata.
        toDelete.forEach( qImg -> qImg.getPageImg().deleteQuestionImg( qImg ) ) ;

        // Rebuild the question repo once. This persists question-info.json
        // without the deleted question.
        questionRepo.refresh() ;
        toDelete.forEach( this::notifyListenersQuestionImgDeletedNoRefresh ) ;

        // Physically delete the files.
        for( QuestionImage qImg : toDelete ) {
            File imgFile = qImg.getImgFile() ;
            if( imgFile.delete() ) {
                log.info( "Deleted question image file: <img-dir>/{}", imgFile.getName() ) ;
            }
            else {
                log.warn( "Failed to delete question image file: <img-dir>/{}", imgFile.getName() ) ;
            }
        }

        removeAITopicMapEntry( question.getQID().toString() ) ;

        if( toDelete.contains( context.getLastSavedImg() ) ) {
            context.setLastSavedImage( getLastQuestionImg() ) ;
        }

        log.info( "Deleted question {}. Part images = {}, LCT context deleted = {}",
                  question.getQRef(), question.getOwnQImgList().size(), deleteLctCtx ) ;
    }

    private QuestionImage getLastQuestionImg() {
        QuestionImage lastQImg = null ;
        for( PageImage pageImg : pageImages ) {
            for( QuestionImage qImg : pageImg.getQImgList() ) {
                if( lastQImg == null ||
                    qImg.getQId().compareTo( lastQImg.getQId() ) > 0 ) {
                    lastQImg = qImg ;
                }
            }
        }
        return lastQImg ;
    }

    private void removeAITopicMapEntry( String qId ) {

        File aiTopicMapFile = new File( workDir, "ai-topic-map.json" ) ;
        if( !aiTopicMapFile.exists() ) {
            return ;
        }

        try {
            JSONObject json = new JSONObject( FileUtils.readFileToString( aiTopicMapFile, "UTF-8" ) ) ;

            // Keys may use '_' as the separator, see AITopicSuggestionRepo
            List<String> keysToRemove = new ArrayList<>() ;
            Iterator<?> keys = json.keys() ;
            while( keys.hasNext() ) {
                String key = ( String )keys.next() ;
                if( key.replace( "_", "/" ).equals( qId ) ) {
                    keysToRemove.add( key ) ;
                }
            }

            if( !keysToRemove.isEmpty() ) {
                keysToRemove.forEach( json::remove ) ;
                FileUtils.writeStringToFile( aiTopicMapFile, json.toString( 2 ), "UTF-8" ) ;
                log.info( "Removed AI topic map entry for {}", qId ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error removing AI topic map entry for {}", qId, e ) ;
        }
    }

    public void setSelectedPageImg( PageImage selectedPageImg ) {
        context.setSelectedPageImg( selectedPageImg ) ;
        for( PageImage pageImage : pageImages ) {
            pageImage.getState().setSelected( pageImage == selectedPageImg );
        }
        savePageState() ;
    }
    
    public void setPageImgClosed( PageImage closedPageImg ) {
        for( PageImage pageImage : pageImages ) {
            if( pageImage == closedPageImg ) {
                pageImage.getState().setVisible( false ) ;
                pageImage.getState().setSelected( false ) ;
            }
        }
        savePageState() ;
    }
    
    public PageImage getSelectedPageImg() {
        for( PageImage pageImage : pageImages ) {
            if( pageImage.getState().isSelected() )
                return pageImage ;
        }
        return null ;
    }
    
    public boolean isManualProject() {
        return baseDir.getName().startsWith( "MN-" ) ;
    }
}
