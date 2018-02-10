package main

import (
	"fmt"
	//"io"
	//"log"
	"github.com/osum4est/awesome-sms-server/database"
	"github.com/osum4est/awesome-sms-server/json"
	"net/http"
)

const (
	port = "11150"

	GET  = "GET"
	POST = "POST"
)

var db *database.DB

func main() {
	fmt.Println("Starting AwesomeSMS server on port", port)

	db = &database.DB{}
	db.Open()

	http.HandleFunc("/status", status)
	http.HandleFunc("/new_message", newMessage)
	fmt.Println(http.ListenAndServe(":"+port, nil))
}

func status(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, GET) {
		sendResponse(w, "Ready", "status")
	}
}

func newMessage(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, POST) {
		var message json.MessageJson
		json.Decode(&r.Body, &message)
		fmt.Println("Got message:", message)

		// Get sender and addresses
		var sender string
		addresses := make([]string, 0)
		for _, address := range message.Addresses {
			if !contains(addresses, address.Address) {
				addresses = append(addresses, address.Address)
			}
			if address.Type == json.MessageAddressTypeFrom {
				sender = address.Address
			}
		}
		// Insert into message table
		err := db.MessageTable.Insert(message.Id, message.Date, message.Protocol, message.ThreadId, sender, message.Body)
		if err != nil {
			sendError(w, err.Error())
			return
		}

		// Insert attachments (1 at a time because it is easier and it's extremely rare to have multiple
		for _, attachment := range message.Attachments {
			db.AttachmentTable.Insert(message.Id, attachment.Mime, attachment.Data)
		}

		// Insert the thread if it doesn't exist
		db.ThreadParticipantTable.InsertIfNotExists(message.ThreadId, addresses)

		sendResponse(w, "Success", "status")
	}
}

// TODO: Do something with this...
func contains(array []string, item string) bool {
	for _, arrayItem := range array {
		if item == arrayItem {
			return true
		}
	}
	return false
}

////////////////////
// HELPER METHODS //
////////////////////

func sendResponse(w http.ResponseWriter, value interface{}, path ...string) {
	var response json.Json
	response.Set(value, path...)
	sendResponseJson(w, response)
}

func sendResponseJson(w http.ResponseWriter, response json.Json) {
	w.Header().Set("Content-Type", "application/json")
	fmt.Fprint(w, response.GetString())
}

func ensureMethod(w http.ResponseWriter, r *http.Request, method string) bool {
	if r.Method != method {
		sendBadMethod(w, method)
		return false
	}
	return true
}

func sendBadMethod(w http.ResponseWriter, method string) {
	sendError(w, "Endpoint requires "+method+" request.")
}

func sendError(w http.ResponseWriter, message string) {
	w.WriteHeader(http.StatusBadRequest)
	sendResponse(w, message, "error", "message")
}
