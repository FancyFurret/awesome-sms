package main

type Message struct {
	Id       int
	Date     int64
	Protocol byte
	ThreadId int
	Sender   int
	Body     string
}
