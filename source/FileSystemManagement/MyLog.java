/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileSystemManagement;

/**
 *
 * @author BThang
 */
import java.util.*;

public class MyLog {
    public static boolean isLog = true;
    
    public static boolean isGui = false;
    
    public static void log(Object o){
        if(isLog){
            System.out.println(o);
        }
    }

    
}
