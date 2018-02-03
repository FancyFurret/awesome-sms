package com.eightbitforest.awesomesms.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Contact {

    private int id;
    private String name;
    private ArrayList<Phone> phones;
    private byte[] photo;

    public Contact(int id, String name, ArrayList<Phone> phones, byte[] photo) {
        this.id = id;
        this.name = name;
        this.phones = phones;
        this.photo = photo;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phones=" + phones +
                ", photo=" + Arrays.toString(photo) +
                '}';
    }

    public static class Phone {

        private String number;
        private int type;

        public Phone(String number, int type) {
            this.number = number;
            this.type = type;
        }

        public String getNumber() {
            return number;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "number='" + number + '\'' +
                    ", type=" + type +
                    '}';
        }
    }
}
