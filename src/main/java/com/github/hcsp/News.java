package com.github.hcsp;

import java.time.Instant;

public class News {
    private Integer id;
    private String title;
    private String content;
    private String url;
    private Instant createAt;
    private Instant modifiedAt;

    public News() {

    }

    public News(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public News(News old) {
        this.id = old.id;
        this.title = old.title;
        this.content = old.content;
        this.url = old.url;
        this.createAt = old.createAt;
        this.modifiedAt = old.modifiedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Instant createAt) {
        this.createAt = createAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
