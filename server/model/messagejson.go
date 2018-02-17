package model

type MessageJson struct {
	Id          int `json:"id"`
	Date        int64 `json:"date"`
	Protocol    byte `json:"protocol"`
	ThreadId    int `json:"threadId"`
	Addresses   []MessageAddressJson `json:"addresses"`
	Body        string `json:"body"`
	Attachments []MessageAttachmentJson `json:"attachments"`
}

const (
	MessageAddressTypeTo   = 0
	MessageAddressTypeFrom = 1
	MessageAddressTypeCC   = 2
)

type MessageAddressJson struct {
	Address string `json:"address"`
	Type    byte `json:"type"`
}

type MessageAttachmentJson struct {
	Id   int `json:"id"`
	Mime string `json:"mime"`
	Data []byte `json:"data"`
}
