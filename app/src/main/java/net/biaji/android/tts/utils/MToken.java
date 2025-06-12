package net.biaji.android.tts.utils;

public class MToken {
    private String text;
    private String tag;
    private String whitespace;
    private String phonemes;

    public MToken(String text, String tag, String whitespace) {
        this.text = text;
        this.tag = tag;
        this.whitespace = whitespace;
    }

    public String getTag() {
        return tag;
    }

    public String getWhitespace() {
        return whitespace;
    }

    public String getPhonemes() {
        return phonemes;
    }

    public void setWhitespace(String w) {
        this.whitespace = w;
    }

    public void appendWhitespace(String w) {
        this.whitespace += w;
    }

    public void setPhonemes(String p) {
        this.phonemes = p;
    }
}