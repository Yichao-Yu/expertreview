package com.yichao.alexa.model;

public class ProductSeller {

    private String seller;
    private String price;
    private String availability;
    private String link;

    public ProductSeller() {
    }

    public ProductSeller(String seller, String price, String availability, String link) {
        this.seller = seller;
        this.price = price;
        this.availability = availability;
        this.link = link;
    }

    public String getSeller() {
        return seller;
    }

    public String getPrice() {
        return price;
    }

    public String getAvailability() {
        return availability;
    }

    public String getLink() {
        return link;
    }
}
