package de.uni_koblenz.javax.lang.model;

import java.lang.ref.WeakReference;
import java.util.HashSet;

public enum SourceVersion {
    RELEASE_0, RELEASE_1, RELEASE_2, RELEASE_3, RELEASE_4, RELEASE_5, RELEASE_6;

    private static WeakReference<HashSet<String>> mKeywords;

    public static boolean isIdentifier(CharSequence name) {
        if(name == null || name.length() == 0) return false;
        if(!Character.isJavaIdentifierStart(name.charAt(0))) return false;

        for(int i = 1; i < name.length(); ++i) {
            if(!Character.isJavaIdentifierPart(name.charAt(i))) return false;
        }

        return true;
    }

    public static boolean isKeyword(CharSequence s) {
        return getKeywords().contains(s);
    }

    public static boolean isName(CharSequence name) {
        if(name == null || name.length() == 0) return false;
        String[] parts = name.toString().split("\\.");

        for(String p : parts) {
            if(!isIdentifier(p)) return false;
        }

        return true;
    }

    public static SourceVersion latest() {
        return RELEASE_6;
    }

    public static SourceVersion latestSupported() {
        return RELEASE_6;
    }

    private static synchronized HashSet<String> getKeywords() {
        HashSet<String> keys = null;

        if(mKeywords != null) {
            keys = mKeywords.get();
        }

        if(keys == null) {
            // List taken from
            // http://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html
            keys = new HashSet<String>();
            keys.add("abstract");
            keys.add("continue");
            keys.add("for");
            keys.add("new");
            keys.add("switch");
            keys.add("assert");
            keys.add("default");
            keys.add("goto");
            keys.add("package");
            keys.add("synchronized");
            keys.add("boolean");
            keys.add("do");
            keys.add("if");
            keys.add("private");
            keys.add("this");
            keys.add("break");
            keys.add("double");
            keys.add("implements");
            keys.add("protected");
            keys.add("throw");
            keys.add("byte");
            keys.add("else");
            keys.add("import");
            keys.add("public");
            keys.add("throws");
            keys.add("case");
            keys.add("enum");
            keys.add("instanceof");
            keys.add("return");
            keys.add("transient");
            keys.add("catch");
            keys.add("extends");
            keys.add("int");
            keys.add("short");
            keys.add("try");
            keys.add("char");
            keys.add("final");
            keys.add("interface");
            keys.add("static");
            keys.add("void");
            keys.add("class");
            keys.add("finally");
            keys.add("long");
            keys.add("strictfp");
            keys.add("volatile");
            keys.add("const");
            keys.add("float");
            keys.add("native");
            keys.add("super");
            keys.add("while");
            mKeywords = new WeakReference<HashSet<String>>(keys);
        }

        return keys;
    }
}
