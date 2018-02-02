package com.eightbitforest.awesomesms.model;

import java.util.ArrayList;

/**
 * Model for a text message. This is what will be created by the TextObserver class. Stores
 * all necessary information about an SMS or MMS message.
 *
 * @author Forrest Jones
 */

public class TextMessage {

    public static final byte PROTOCOL_SMS = 0;
    public static final byte PROTOCOL_MMS = 1;

    public static final byte BOX_INBOX = 0;
    public static final byte BOX_SENT = 1;

    private int id;
    private String message;
    private ArrayList<Address> addresses;
    private ArrayList<Attachment> attachments;
    private int thread;
    private long date;
    private byte protocol;
    private byte box;

    public TextMessage(int id, String message, ArrayList<Address> addresses, ArrayList<Attachment> attachments, int thread, long date, byte protocol, byte box) {
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

    public byte getProtocol() {
        return protocol;
    }

    public byte getBox() {
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

        public static final byte TYPE_TO = 0;
        public static final byte TYPE_FROM = 1;
        public static final byte TYPE_CC = 2;

        private String address;
        private byte type;

        public Address(String address, byte type) {
            this.address = address;
            this.type = type;
        }

        public String getAddress() {
            return address;
        }

        public byte getType() {
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
