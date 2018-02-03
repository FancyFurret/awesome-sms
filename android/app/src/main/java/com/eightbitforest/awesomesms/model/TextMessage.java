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

    /**
     * Constructs a TextMessage.
     *
     * @param id          The id of the message.
     * @param message     The body of the message.
     * @param addresses   List of addresses associated with contacts in the conversation.
     * @param attachments List of attachments sent with this message.
     * @param thread      The id of the conversation.
     * @param date        When the message was sent.
     * @param protocol    SMS or MMS.
     * @param box         Sent or received.
     */
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

    /**
     * Helper class to store necessary information about an address.
     */
    public static class Address {

        public static final byte TYPE_TO = 0;
        public static final byte TYPE_FROM = 1;
        /** Used for other people in a group message */
        public static final byte TYPE_CC = 2;

        private String address;
        private byte type;

        /**
         * Constructs an Address.
         *
         * @param address The phone number.
         * @param type    The type (TYPE_TO, TYPE_FROM, TYPE_CC)
         */
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

    /**
     * Helper class to store necessary information about an attachment.
     */
    public static class Attachment {
        private String type;
        private byte[] data;

        /**
         * Constructs an Attachment.
         *
         * @param type The mime type of the attachment.
         * @param data The raw data of the attachment.
         */
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
