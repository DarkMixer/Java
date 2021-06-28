/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.worldwidewhat.server.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lh
 */
public class Reader {
    private final InputStream input;
    
    public Reader(InputStream input){
        this.input = input;
    }
    
    public String getHeader(){
        String header = "";
        try {
            int cntNew = 0;
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            while(input.available() > 0){
                byte d[] = input.readNBytes(1);
                if(d[0] == '\r') continue;
                data.write(d);
                if(d[0] == '\n'){
                    cntNew++;
                    if(cntNew >= 2) break;
                }else{
                    cntNew = 0;
                }
            }
            header = new String(data.toByteArray());
            
        }catch(Exception ex){
            Logger.getLogger(Reader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return header;
    }
    
    public String[] getHeaderLines(){
        return getHeader().split("\n");
    }
    
    public String read(){
        String retval = "";
        try {
            if(input.available() > 0){
                byte arr[] = new byte[input.available()];
                input.read(arr);
                retval = new String(arr);
            }
        } catch (IOException ex) {
            Logger.getLogger(Reader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retval;
    }
    
    public String[] readLines(){
        String data = read();
        //System.out.println(data);
        data = data.replaceAll("\r", "");
        return data.split("\n");
    }
 
    public byte[] getAvailable(){
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            while(input.available() > 0){
                byte arr[] = new byte[input.available()];
                input.read(arr);
                data.writeBytes(arr);
                try {
                    int wait = 10;
                    while(wait > 0){
                        if(input.available() > 0) break;
                        Thread.sleep(100);
                        wait--;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Reader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
        } catch(Exception ex) {
            Logger.getLogger(Reader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data.toByteArray();
    }
 
    private boolean wait(int cnt){
        int waitCnt = cnt;
        try{
            while(waitCnt > 0){
                if(input.available() > 0) return false;
                Thread.sleep(100);
                waitCnt--;
            }
        } catch(Exception ex){
        }
        return true;
    }
    
    public byte[] getData(int length, String boundry){
        byte retData[] = new byte[length];
        try {
            String strRead = "";
            if(wait(100)) return null;
            System.out.println("Fetch data");
            while(input.available() > 0){
                byte d[] = input.readNBytes(1);
                strRead += new String(d);
                if(d[0] == '\n') {
                    if(strRead.contains(boundry)) break;
                }
                if(wait(10)) break;
            }
            
            if(strRead.contains(boundry)){
                strRead = "";
                if(wait(10)) return null;
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                
                while(input.available() > 0) {
                    byte b[] = input.readNBytes(1);
                    data.write(b);
                    if(b[0] == '\n'){
                        if(strRead.contains(boundry)) break;
                        else strRead = "";
                        
                    } else {
                        strRead += new String(b);
                        if(strRead.length() > boundry.length() + 20){
                            strRead = strRead.substring(strRead.length() - boundry.length());
                        }
                    }
                    if(wait(5)) break;
                }
                
                if(strRead.contains(boundry)) {
                    retData = new byte[data.size() - (strRead.length() + 1)];
                    System.arraycopy(data.toByteArray(), 0, retData, 0, retData.length);
                } else {
                    retData = data.toByteArray();
                }
            }
            
        } catch (Exception ex){
            Logger.getLogger(Reader.class.getName()).log(Level.SEVERE, null, ex);
            retData = null;
        }
        return retData;
    }
}
