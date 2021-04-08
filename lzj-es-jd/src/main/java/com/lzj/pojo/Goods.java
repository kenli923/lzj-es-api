package com.lzj.pojo;

public class Goods {
    private String title;
    private String price;
    private String img;

    public Goods() {
    }

    public Goods(String title, String price, String img) {
        this.title = title;
        this.price = price;
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", img='" + img + '\'' +
                '}';
    }
}
