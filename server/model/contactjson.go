package model

type ContactJson struct {
	Id        int                `json:"id"`
	Name      string             `json:"name"`
	Phones    []ContactPhoneJson `json:"phones"`
	Thumbnail []byte             `json:"thumbnail"`
}

type ContactPhoneJson struct {
	Number string `json:"number"`
	Type   int    `json:"type"`
}
