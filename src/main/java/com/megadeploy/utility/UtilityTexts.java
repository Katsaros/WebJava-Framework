package com.megadeploy.utility;

public class UtilityTexts {
    private UtilityTexts() {
    }

    public static final String WEB_JAVA = "[WebJava]";
    public static final String APP = "[App]";
    public static final String OUTPUT = "\n[Output]";

    public static void logWebJava(String message) {
        System.out.println(WEB_JAVA +"["+message+"]");
    }

    public static void logApp(String message) {
        System.out.println(APP +"["+message+"]");
    }

    public static void logOutput(String output) {
        System.out.println(OUTPUT +output+"\n");
    }
}
