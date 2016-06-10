package models;

import com.avaje.ebean.Model;


public class File extends Model {

    private String name;

    private String title;

    private String description;

    private String url;

    private String owner;

    private String id;

    private String tags;

    private String categories;

    private String publisher;

    private String number;

    private String issuingDate;

    public String getIssuingDate() { return issuingDate; }

    public void setIssuingDate(String issuingDate) { this.issuingDate = issuingDate; }

    public String getNumber() { return number; }

    public void setNumber(String number) { this.number = number; }

    public String getPublisher() { return publisher; }

    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) { this.tags = tags; }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    private String thumbnail;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.substring(0, Math.min(name.length(), 40));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getId(){ return id;}
    public void setId (String id){ this.id = id;}


    public String getDescription(){ return description;}
    public void setDescription(String desc){ this.description = desc;}


}
