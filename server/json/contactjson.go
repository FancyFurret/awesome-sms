package json

type ContactJson struct {
	Id        int
	Name      string
	Phones    []ContactPhoneJson
	Thumbnail []byte
}

type ContactPhoneJson struct {
	Number string
	Type   int
}
