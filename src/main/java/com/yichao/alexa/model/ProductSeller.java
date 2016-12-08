package com.yichao.alexa.model;

public class ProductSeller {

    private String seller;
    private String price;
    private String availability;

    public ProductSeller(String seller, String price, String availability) {
        this.seller = seller;
        this.price = price;
        this.availability = availability;
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
}
