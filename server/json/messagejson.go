package json

type MessageJson struct {
	Id          int
	Date        int64
	Protocol    byte
	ThreadId    int
	Addresses   []MessageAddressJson
	Body        string
	Attachments []MessageAttachmentJson
}

const (
	MessageAddressTypeTo   = 0
	MessageAddressTypeFrom = 1
	MessageAddressTypeCC   = 2
)

type MessageAddressJson struct {
	Type    byte
	Address string
}

type MessageAttachmentJson struct {
	Mime string
	Data []byte
}
