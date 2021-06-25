/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.worldwidewhat.server.net;

import dk.worldwidewhat.app.IApplication;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lh
 */
public class NetworkClient extends Thread {
    //region Private declares
    private Socket _socket;
    private int _stage = 0;
    private String _requestPath;
    
    //endregion
    
    /** Class constructor
     * @param socket Client socket */
    public NetworkClient(Socket socket){
        _socket = socket;
    }
    
   
    public void close(){
        if(_socket != null) {
            try{
                _socket.close();
                _socket = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean testLine(String line){
        
        if(_stage == 0) {
            String strArr[] = line.split(" ");
            if(strArr.length != 3) return false;
            if(strArr[0].equals("GET") || strArr[0].equals("POST")) {
                if(strArr[1].isEmpty()) return false;
                _requestPath = strArr[1];
                if(!strArr[2].equals("HTTP/1.1")) return false;
            } else return false;
        } else {
            
        }
        return true;
    }
    
    /** Network client thread run */
    @Override
    public void run() {
        try {
            // Get the sockets input stream
            BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            // Attach output stream to string writer
            PrintWriter out = new PrintWriter(_socket.getOutputStream());
            System.out.println("Client connected");
            out.flush();
            String line = in.readLine();
            while(line != null & !line.isEmpty()) {
                System.out.println(line);
                if(testLine(line)) {
                    _stage++;
                } else {
                    _stage = 0;
                    break;
                }
                line = in.readLine();
            }

            Date date = new Date();  
            SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

            if(_stage == 0 || _requestPath.length() < 2 || !_requestPath.startsWith(("/"))){
                out.println("HTTP/1.1 400 Bad Request");
            } else {
                String classPath = "dk.worldwidewhat.app";

                String args = "";
                if(_requestPath.contains("?")){
                    int pos = _requestPath.indexOf("?", 0);
                    args = _requestPath.substring(pos+1);
                    _requestPath = _requestPath.substring(0, pos);
                }
                
                String strArr[] = _requestPath.split("/");
                System.out.println("RequestPath: " + _requestPath);
                if(strArr.length > 1){
                    
                    for(int i=1; i < strArr.length-1; i++){
                        classPath += "." + strArr[i];
                    }
                    String className = strArr[strArr.length - 1].substring(0,1).toUpperCase() + strArr[strArr.length - 1].substring(1);
                    classPath += "." + className;
                    System.out.println("Path: " + classPath);

                    try {
                        Class<?> tmpClass = Class.forName(classPath);
                        System.out.println("Class loaded");
                        IApplication cls = (IApplication) tmpClass.getDeclaredConstructor().newInstance();
                        System.out.println("Class cast");
                        String response = cls.execute(args);

                        out.println("HTTP/1.1 200 OK\n" + 
                                    "Date: " + formatter.format(date) + "\n" +
                                    "Server: BirdFlipper\n" +
                                    "Content-Length: " + response.length() + "\n" +
                                    "Connection: close\n" +
                                    "Content-Type: application/json; charset=iso-8859-1\n" +
                                    "\n" + response);                        
                        
                    } catch(Exception fex) {
                        out.println("HTTP/1.1 404 Not Found");
                    }
                    
                } else {
                    out.println("HTTP/1.1 400 Bad Request");
                }
            }
            out.flush();

            in.close();
            out.close();        
            
        } catch (Exception ex) {
            Logger.getLogger(NetworkClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        close();
    }    
}
