package com.eightbitforest.awesomesms.model;

import java.util.ArrayList;

/**
 * Model for a text message. This is what will be created by the TextObserver class. Stores
 * all necessary information about an SMS or MMS message.
 *
 * @author Forrest Jones
 */

public class TextMessage {

    public static final int PROTOCOL_SMS = 0;
    public static final int PROTOCOL_MMS = 1;

    public static final int BOX_INBOX = 0;
    public static final int BOX_SENT = 1;

    private int id;
    private int thread;
    private ArrayList<Address> addresses;
    private String message;
    private long date;
    private int protocol;
    private int box;

    public TextMessage(int id, int thread, ArrayList<Address> addresses, String message, long date, int protocol, int box) {
        this.id = id;
        this.thread = thread;
        this.addresses = addresses;
        this.message = message;
        this.date = date;
        this.protocol = protocol;
        this.box = box;
    }

    public int getId() {
        return id;
    }

    public int getThread() {
        return thread;
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }

    public String getMessage() {
        return message;
    }

    public long getDate() {
        return date;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getBox() {
        return box;
    }

    @Override
    public String toString() {
        return "TextMessage{" +
                "id=" + id +
                ", thread=" + thread +
                ", addresses=" + addresses +
                ", message='" + message + '\'' +
                ", date=" + date +
                ", protocol=" + protocol +
                ", box=" + box +
                '}';
    }

    public static class Address {

        public static final int TYPE_TO = 0;
        public static final int TYPE_FROM = 1;
        public static final int TYPE_CC = 2;

        private String address;
        private int type;

        public Address(String address, int type) {
            this.address = address;
            this.type = type;
        }

        public String getAddress() {
            return address;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "address='" + address + '\'' +
                    ", type=" + type +
                    '}';
        }
    }
}
