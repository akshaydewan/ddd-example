package com.thoughtworks.domain.model;

public class Item {

    private final int quantity;
    private final String itemId;

    public Item(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }


    public String getItemId() {
        return itemId;
    }
}
