package com.eightbitforest.awesomesms.model;

import android.util.Base64;

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
    private String body;
    private ArrayList<Address> addresses;
    private ArrayList<Attachment> attachments;
    private int threadId;
    private long date;
    private byte protocol;

    /**
     * Constructs a TextMessage.
     *
     * @param id          The id of the message.
     * @param body        The body of the message.
     * @param addresses   List of addresses associated with contacts in the conversation.
     * @param attachments List of attachments sent with this message.
     * @param threadId    The id of the conversation.
     * @param date        When the message was sent.
     * @param protocol    SMS or MMS.
     */
    public TextMessage(int id, String body, ArrayList<Address> addresses, ArrayList<Attachment> attachments, int threadId, long date, byte protocol) {
        this.id = id;
        this.body = body;
        this.addresses = addresses;
        this.attachments = attachments;
        this.threadId = threadId;
        this.date = date;
        this.protocol = protocol;
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public int getThreadId() {
        return threadId;
    }

    public long getDate() {
        return date;
    }

    public byte getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return "TextMessage{" +
                "id=" + id +
                ", body='" + body + '\'' +
                ", addresses=" + addresses +
                ", attachments=" + attachments +
                ", threadId=" + threadId +
                ", date=" + date +
                ", protocol=" + protocol +
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
        private int id;
        private String mime;
        private byte[] data;

        /**
         * Constructs an Attachment.
         *
         * @param id   The id of the attachment.
         * @param mime The mime type of the attachment.
         * @param data The raw data of the attachment.
         */
        public Attachment(int id, String mime, byte[] data) {
            this.id = id;
            this.mime = mime;
            this.data = Base64.encode(data, Base64.DEFAULT);
        }

        public int getId() {
            return id;
        }

        public String getMime() {
            return mime;
        }

        public byte[] getData() {
            return data;
        }

        @Override
        public String toString() {
            return "Attachment{" +
                    "id=" + id +
                    ", mime='" + mime + '\'' +
                    '}';
        }
    }
}
