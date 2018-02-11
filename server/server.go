package main

import (
	"fmt"
	//"io"
	//"log"
	"github.com/osum4est/awesome-sms-server/database"
	"github.com/osum4est/awesome-sms-server/json"
	"github.com/osum4est/awesome-sms-server/model"
	"net/http"
)

const (
	port = "11150"

	GET    = "GET"
	POST   = "POST"
	DELETE = "DELETE"
)

var db *database.DB

func main() {
	fmt.Println("Starting AwesomeSMS server on port", port)

	db = &database.DB{}
	db.Open()

	http.HandleFunc("/status", status)
	http.HandleFunc("/insert_message", insertMessage)
	http.HandleFunc("/insert_messages", insertMessages)
	http.HandleFunc("/update_contact", updateContact)
	http.HandleFunc("/update_contacts", updateContacts)
	http.HandleFunc("/delete_contact", deleteContact)
	http.HandleFunc("/delete_contacts", deleteContacts)
	fmt.Println(http.ListenAndServe(":"+port, nil))
}

func status(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, GET) {
		sendResponse(w, "Ready", "status")
	}
}

func insertMessage(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, POST) {
		var message model.MessageJson
		json.Decode(&r.Body, &message)
		fmt.Println("Got message:", message)

		db.MessageTable.Insert(&message)
		db.AttachmentTable.InsertFromMessages(&message)
		db.ThreadParticipantTable.InsertFromMessages(&message)

		sendSuccess(w)
	}
}

func insertMessages(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, POST) {
		var messages []*model.MessageJson
		json.Decode(&r.Body, &messages)
		fmt.Println("Got messages:", messages)

		db.MessageTable.Insert(messages...)
		db.AttachmentTable.InsertFromMessages(messages...)
		db.ThreadParticipantTable.InsertFromMessages(messages...)

		sendSuccess(w)
	}
}

func updateContact(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, POST) {
		var contact model.ContactJson
		json.Decode(&r.Body, &contact)
		fmt.Println("Got contact:", contact)

		db.ContactTable.Insert(&contact)
		db.ContactPhoneTable.DeleteFromContacts(&contact)
		db.ContactPhoneTable.InsertFromContacts(&contact)

		sendSuccess(w)
	}
}

func updateContacts(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, POST) {
		var contacts []*model.ContactJson
		json.Decode(&r.Body, &contacts)
		fmt.Println("Got contacts:", contacts)

		db.ContactTable.Insert(contacts...)
		db.ContactPhoneTable.DeleteFromContacts(contacts...)
		db.ContactPhoneTable.InsertFromContacts(contacts...)

		sendSuccess(w)
	}
}

func deleteContact(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, DELETE) {
		var contact model.ContactJson
		json.Decode(&r.Body, &contact)
		fmt.Println("Deleting contact:", contact)

		db.ContactTable.Delete(&contact)
		db.ContactPhoneTable.DeleteFromContacts(&contact)

		sendSuccess(w)
	}
}

func deleteContacts(w http.ResponseWriter, r *http.Request) {
	if ensureMethod(w, r, DELETE) {
		var contacts []*model.ContactJson
		json.Decode(&r.Body, &contacts)
		fmt.Println("Deleting contacts:", contacts)

		db.ContactTable.Delete(contacts...)
		db.ContactPhoneTable.DeleteFromContacts(contacts...)

		sendSuccess(w)
	}
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

func sendSuccess(w http.ResponseWriter) {
	sendResponse(w, "Success", "status")
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
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadRequest)
	sendResponse(w, message, "error", "message")
}
