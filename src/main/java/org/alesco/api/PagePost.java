package org.alesco.api;

public class PagePost {

    private String message;

    private String picture;

    private Integer likes;

    private String link;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PagePost{");
        sb.append("message='").append(message).append('\'');
        sb.append(", picture='").append(picture).append('\'');
        sb.append(", likes=").append(likes);
        sb.append(", link='").append(link).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
