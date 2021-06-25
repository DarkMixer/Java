/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.worldwidewhat.server.net;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lh
 */
public class NetworkInterface extends Thread {
    private final int _port;
    private boolean _running = false;
    private NetworkClient _networkClients[] = new NetworkClient[10];

    
    public NetworkInterface(int port) { _port = port; }
    
    public boolean isRunning(){ return _running; }
    
    public void stopRunning() {
        _running = false;
        this.interrupt();
    }

    private int getAvailableClient(){
        for(int i=0; i < _networkClients.length; i++) {
            if(_networkClients[i] == null || !_networkClients[i].isAlive())
                return i;
        }
        return -1;
    }    
    

    @Override
    public void run(){
        _running = true;
        while(_running) {
            try(ServerSocket serverSocket = new ServerSocket(_port)) {
                Socket socket = serverSocket.accept();
                int index = getAvailableClient();
                if(index < 0) {
                    socket.close();
                } else {
                    _networkClients[index] = new NetworkClient(socket);
                    _networkClients[index].start();
                }
                
            } catch(Exception ex) {
                Logger.getLogger(NetworkInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for(int i=0; i < _networkClients.length; i++) {
            if(_networkClients[i] != null){
                _networkClients[i].close();
                _networkClients[i].interrupt();
                _networkClients[i] = null;
            }
        }        
    }
/*    
    @Override
    protected void finalize() throws Throwable {
        _running = false;
        for(int i=0; i < _networkClients.length; i++) {
            if(_networkClients[i] != null){
                _networkClients[i].close();
                _networkClients[i].interrupt();
                _networkClients[i] = null;
            }
        }
        super.finalize();
    }    
*/
}
