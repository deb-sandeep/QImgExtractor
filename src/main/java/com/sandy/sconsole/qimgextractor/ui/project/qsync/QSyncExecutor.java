package com.sandy.sconsole.qimgextractor.ui.project.qsync;

import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs question syncs in parallel, with two rules that keep the server side
 * safe:
 *
 * 1. All LCT questions are synced one after the other on a single thread.
 *    The questions of an LCT context share the context image file, which
 *    the server deletes and rewrites on every save. Two of them saved at
 *    the same time can leave the file missing.
 * 2. A question is never in flight twice. Concurrent saves of the same
 *    question can make the server create duplicate rows.
 *
 * Other questions are synced on a pool of the configured size.
 *
 * Idle threads time out, so an instance needs no shutdown when its
 * project is closed.
 */
@Slf4j
public class QSyncExecutor {

    private static final long IDLE_TIMEOUT_SECS = 30 ;

    private final ThreadPoolExecutor lctExecutor ;
    private final ThreadPoolExecutor generalExecutor ;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet() ;

    public QSyncExecutor( int numThreads ) {
        int n = Math.max( 1, numThreads ) ;
        this.lctExecutor     = createExecutor( 1, "qsync-lct" ) ;
        this.generalExecutor = createExecutor( n, "qsync" ) ;
        log.debug( "QSync executor created with {} general threads.", n ) ;
    }

    private static ThreadPoolExecutor createExecutor( int numThreads, String namePrefix ) {
        AtomicInteger seq = new AtomicInteger( 0 ) ;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                numThreads, numThreads,
                IDLE_TIMEOUT_SECS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread( r, namePrefix + "-" + seq.incrementAndGet() ) ;
                    t.setDaemon( true ) ;
                    return t ;
                } ) ;
        executor.allowCoreThreadTimeOut( true ) ;
        return executor ;
    }

    /**
     * Queues the sync task of the question.
     *
     * @return the task's future, or null if the question is already queued
     *         or being synced. The task is then not queued.
     */
    public Future<?> submit( Question question, Runnable syncTask ) {
        String key = question.getQRef() ;
        if( !inFlight.add( key ) ) {
            return null ;
        }

        ExecutorService executor = question.isLCT() ? lctExecutor : generalExecutor ;
        try {
            return executor.submit( () -> {
                try {
                    syncTask.run() ;
                }
                finally {
                    inFlight.remove( key ) ;
                }
            } ) ;
        }
        catch( RejectedExecutionException e ) {
            inFlight.remove( key ) ;
            throw e ;
        }
    }
}
