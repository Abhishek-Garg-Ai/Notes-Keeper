package com.abhigarg.notepadapp.model;

public class Note {
    private String title;
    private String content;
    private int textColor;
    private int backgroundColor;

    private Note() {
    }

    public Note(String title, String content, int textColor,int backgroundColor) {
        this.title = title;
        this.textColor = textColor;
        this.content = content;
        this.backgroundColor=backgroundColor;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getBackgroundColor(){
        return backgroundColor;
    }
    public int getTextColor() {
        return textColor;
    }

    public String getContent() {
        return content;
    }

    public void setBackgroundColor(){
        this.setBackgroundColor();
    }
    public void setTextColor() {
        this.setTextColor();
    }

    public void setContent(String content) {
        this.content = content;
    }
}