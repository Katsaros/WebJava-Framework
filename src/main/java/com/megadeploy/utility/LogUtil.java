package com.megadeploy.utility;

import static com.megadeploy.utility.TextUtil.*;

public class LogUtil {
    private LogUtil() {
    }

    public static void logWebJava(String message) {
        System.out.println("\n"+WEB_JAVA +"["+message+"]");
    }

    public static void logApp(String message) {
        System.out.println(APP +"["+message+"]");
    }

    public static void logOutput(String output) {
        System.out.println("\n"+OUTPUT +output+"\n");
    }
}
