/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.worldwidewhat.watcher;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File system watcher 
 * @author cintix.dk
 */
public class Watcher extends Thread {
    private static WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private static boolean running = false;
    
    public Watcher() {
        keys = new LinkedHashMap<>();
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            Logger.getLogger(Watcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run(){
        running = true;
        while(running) {
           // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                break;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == OVERFLOW) continue; 

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                Path child = dir.resolve(filename);
                
                if (kind == ENTRY_CREATE) {
                    System.out.println("CREATION of " + child.toString());
                } else if (kind == ENTRY_MODIFY) {
                    System.out.println("MODIFICATION of " + child.toString());
                } else if (kind == ENTRY_DELETE) {
                    System.out.println("DELETE of " + child.toString());
                }
            }

            if (!key.reset() || !running) break; 
        }
    }

    /** Register the given directory with the WatchService 
     * @param dir Directory to add to watcher
     * @throws IOException */
    public void register(Path dir) throws IOException {
        register(dir, false);
    }
    
    /** Register the given directory with the WatchService, and crawl
     *  through sub directories.
     * @param dir Root directory to add to watcher
     * @param crawl Crawl through sub directories
     * @throws IOException 
     */
    public void register(Path dir, boolean crawl) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        keys.put(key, dir);
        if(crawl) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(Files.isDirectory(file)) {
                        System.out.println(file.toString());
                        WatchKey subKey = file.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                        keys.put(subKey, file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
