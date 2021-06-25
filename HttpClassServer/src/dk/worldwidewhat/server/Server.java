/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.worldwidewhat.server;

import dk.worldwidewhat.server.net.NetworkInterface;
import dk.worldwidewhat.watcher.Watcher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lh
 */
public class Server {
    private final int _serverPort = 8090;
    private NetworkInterface _networkInterface;
    private Watcher _watcher;
    
    public Server(){
        
    }
    
    public void init(){
        try{
            _networkInterface = new NetworkInterface(_serverPort);
            _watcher = new Watcher();
            _watcher.register(new File("/home/lh/workspace/git/WorldWideWhat/Java").toPath(), true);
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);            
        }                
    }
    
    public void start(){
        if(_networkInterface == null) return;
        if(_networkInterface.isRunning()) return;
        try {
            _networkInterface.start();
            _watcher.start();
            
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    public static void main(String args[]) {
        
        Server server = new Server();
        server.init();
        server.start();
    }
   
}
