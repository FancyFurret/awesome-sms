package com.eightbitforest.awesomesms.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Contact {

    private int id;
    private String name;
    private ArrayList<String> addresses;
    private byte[] photo;

    public Contact(int id, String name, ArrayList<String> addresses, byte[] photo) {
        this.id = id;
        this.name = name;
        this.addresses = addresses;
        this.photo = photo;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", addresses=" + addresses +
                ", photo=" + Arrays.toString(photo) +
                '}';
    }
}
