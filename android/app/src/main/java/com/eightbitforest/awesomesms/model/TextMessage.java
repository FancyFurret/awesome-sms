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
    private String message;
    private ArrayList<Address> addresses;
    private ArrayList<Attachment> attachments;
    private int thread;
    private long date;
    private int protocol;
    private int box;

    public TextMessage(int id, String message, ArrayList<Address> addresses, ArrayList<Attachment> attachments, int thread, long date, int protocol, int box) {
        this.id = id;
        this.message = message;
        this.addresses = addresses;
        this.attachments = attachments;
        this.thread = thread;
        this.date = date;
        this.protocol = protocol;
        this.box = box;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public int getThread() {
        return thread;
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
                ", message='" + message + '\'' +
                ", addresses=" + addresses +
                ", attachments=" + attachments +
                ", thread=" + thread +
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

    public static class Attachment {
        private String type;
        private byte[] data;

        public Attachment(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public byte[] getData() {
            return data;
        }

        @Override
        public String toString() {
            return "Attachment{" +
                    "type='" + type + '\'' +
                    '}';
        }
    }
}
