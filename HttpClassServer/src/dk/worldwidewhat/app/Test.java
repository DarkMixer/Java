/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.worldwidewhat.app;

/**
 *
 * @author lh
 */
public class Test implements IApplication {

    /** Execute class with data
     * Function is attached to IExtension interface
     * @param data class payload
     * @return Response data */
    public String execute(String data){
        
        System.out.println("Execute test");
        String strData = "{ \"company\": \"WorldWideWhat\", " +
                         "\"class\": \"" + Test.class.getName() + "\"";
        
        if(data.length() > 0) {
            if(data.contains("&")) {
                String arrArg[] = data.split("&");
                strData += ", \"arguments\":[";
                for(int i=0; i < arrArg.length; i++){
                    if(i>0) strData +=", ";
                    if(arrArg[i].contains("=")){
                        int pos = arrArg[i].indexOf("=");
                        strData += "{\"" + arrArg[i].substring(0, pos) + 
                                    "\":\"" + arrArg[i].substring(pos + 1) + "\"}";
                    } else {
                        strData += "\"" + arrArg[i] + "\"";
                    }
                }
                strData += "]";
            } else {
                strData +=", \"data\": \"" + data + "\"";
            }
        }
        strData +="}";
        return strData;
    }
    /** Execute class without data
     * Function is attached to IExtension interface 
     * @return Response data */    
    public String execute(){
        return execute("");
    }    
    
}
