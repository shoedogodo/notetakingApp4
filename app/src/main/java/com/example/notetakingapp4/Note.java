package com.example.notetakingapp4;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Note {
    String title;
    String content;
    Timestamp timestamp;
    String category;
    List<String> mediaUrls;
    List<Integer> urlTypes;
    List<String> keywords;

    public Note() {
        mediaUrls = new ArrayList<>();
        urlTypes = new ArrayList<>();
    }

    public List<String> splitIntoKeywords(String text) {
        text = text.toLowerCase(); // 转换为小写
        text = text.replaceAll("[^a-zA-Z0-9\\s]", ""); // 去除标点
        List<String> keywords = Arrays.asList(text.split("\\s+")); // 分割单词
        // 过滤停用词（如果需要）
        List<String> stopWords = Arrays.asList("a", "an", "the", "and", "but", "or", "for", "nor", "on", "at", "to", "from", "by");
        return keywords.stream().filter(k -> !stopWords.contains(k)).collect(Collectors.toList());
    }

    public void renewKeywords(){
        String fullContext = title + " " + content;
        keywords = splitIntoKeywords(fullContext);
    }
    public List<String> getKeywords(){
        return keywords;
    }
    public void addMediaUrl(String mediaUrl, int type) {
        this.mediaUrls.add(mediaUrl);
        this.urlTypes.add(type);
    }

    public List<Integer> getUrlTypes(){return urlTypes;}
    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public String getCategory(){return  category;}

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
