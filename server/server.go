package main

import (
	"fmt"
	//"io"
	//"log"
	"github.com/osum4est/awesome-sms-server/database"
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
		var message Message
		Decode(&r.Body, &message)

		fmt.Println("Got message:", message)
		sendResponse(w, "Success", "status")
	}
}

////////////////////
// HELPER METHODS //
////////////////////

func sendResponse(w http.ResponseWriter, value interface{}, path ...string) {
	var response json
	response.Set(value, path...)
	sendResponseJson(w, response)
}

func sendResponseJson(w http.ResponseWriter, response json) {
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
