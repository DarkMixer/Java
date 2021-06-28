/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.worldwidewhat.server.net;

import dk.worldwidewhat.server.io.Reader;
import dk.worldwidewhat.app.IApplication;
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
public class NetworkClient extends Thread implements AutoCloseable {
    //region Private declares
    private Socket _socket;
    private int _stage = 0;
    private String _requestPath;
    private String _htmlVersion;
    
    private int _contentLength;
    private String _boundary;
    
    //endregion
    
    /** Class constructor
     * @param socket Client socket */
    public NetworkClient(Socket socket){ _socket = socket; }
    
    @Override
    public void close() throws Exception {
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
        System.out.println(line);
        if(_stage == 0) {
            String strArr[] = line.split(" ");
            if(strArr.length != 3) return false;
            if(strArr[0].equals("GET") || strArr[0].equals("POST")) {
                if(strArr[1].isEmpty()) return false;
                _requestPath = strArr[1];
                if(strArr[2].equals("HTTP/1.1") || strArr[2].equals("HTTP/1.0"))
                    _htmlVersion = strArr[2];
                else return false;
            } else return false;
        } else {
            if(line.startsWith("Content-Length:") && _contentLength == 0){
                _contentLength = Integer.parseInt(line.split(":")[1].trim());
            } else if(line.startsWith("Content-Type:")) {
                String arrContType[] = line.substring(line.indexOf(":") + 1).split(";");
                if(arrContType.length > 1) {
                    if(arrContType[0].trim().equals("multipart/form-data")){
                        if(arrContType[1].trim().startsWith("boundary")){
                            _boundary = arrContType[1].substring(arrContType[1].indexOf("=")+1).trim();
                        } else { 
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    
    /** Network client thread run */
    @Override
    public void run() {
        try {
            // Get the sockets input stream
            //BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            // Attach output stream to string writer
            PrintWriter out = new PrintWriter(_socket.getOutputStream());
            System.out.println("Client connected");
            out.flush();
            String lines[] = new Reader(_socket.getInputStream()).getHeaderLines();
            
            for(int i=0; i < lines.length; i ++) {
                if(testLine(lines[i])) {
                    _stage++;
                } else {
                    _stage = 0;
                    break;
                }
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
                        if(IApplication.class.isAssignableFrom(tmpClass)){
                            IApplication cls = (IApplication) tmpClass.getDeclaredConstructor().newInstance();
                            byte data[] = null;
                            if(_contentLength > 0 && _boundary.length() > 0) {
                                data = new Reader(_socket.getInputStream()).getData(_contentLength, _boundary);
                            }
                            
                            String response = cls.execute(args, data);

                            out.println(_htmlVersion + " 200 OK\n" + 
                                        "Date: " + formatter.format(date) + "\n" +
                                        "Server: BirdFlipper\n" +
                                        "Content-Length: " + response.length() + "\n" +
                                        "Connection: close\n" +
                                            "Content-Type: application/json; charset=utf-8\n" +
                                        "\n" + response);                             
                        }else{
                             out.println(_htmlVersion + " 404 Not Found");
                        }
                       
                        
                    } catch(Exception fex) {
                        out.println(_htmlVersion + " 404 Not Found");
                    }
                    
                } else {
                    out.println("HTTP/1.1 400 Bad Request");
                }
            }
            out.flush();
            out.close();        
            close();
        } catch (Exception ex) {
            Logger.getLogger(NetworkClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
