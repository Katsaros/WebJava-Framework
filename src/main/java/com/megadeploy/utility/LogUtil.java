package com.megadeploy.utility;

import static com.megadeploy.utility.TextUtil.*;

public class LogUtil {
    private LogUtil() {
    }

    public static void logWebJavaN(String message) {
        System.out.println("\n"+WEB_JAVA +"["+message+"]");
    }

    public static void logWebJava(String message) {
        System.out.println(WEB_JAVA +"["+message+"]");
    }

    public static void logConfig(String message) {
        System.out.println(CONFIG +"["+message+"]");
    }

    public static void logWebJava(String text, String link) {
        System.out.println(WEB_JAVA +"["+text+" "+link+"]");
    }

    public static void logApp(String message) {
        System.out.println(APP +"["+message+"]");
    }

    public static void logOutput(String output) {
        System.out.println("\n"+OUTPUT +output+"\n");
    }
}
