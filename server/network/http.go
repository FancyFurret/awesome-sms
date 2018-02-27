package network

import(
	"net/http"
	"github.com/osum4est/awesome-sms-server/database"
	"github.com/osum4est/awesome-sms-server/model"
	"github.com/osum4est/awesome-sms-server/json"
	"fmt"
)

const(
	GET    = "GET"
	POST   = "POST"
	DELETE = "DELETE"
)


type httpServer struct {
	port       string
	db         *database.DB
	websockets *websocketServer
}

func NewHttpServer(port string, db *database.DB) *httpServer {
	return &httpServer{port, db, NewWebSocketServer(db)}
}

func (server *httpServer) Start() {

	http.HandleFunc("/status", server.status)
	http.HandleFunc("/insert_message", server.insertMessage)
	http.HandleFunc("/insert_messages", server.insertMessages)
	http.HandleFunc("/update_contact", server.updateContact)
	http.HandleFunc("/update_contacts", server.updateContacts)
	http.HandleFunc("/delete_contact", server.deleteContact)
	http.HandleFunc("/delete_contacts", server.deleteContacts)

	// WebSockets
	http.HandleFunc("/ws", server.websockets.newConnection)


	http.ListenAndServe(":"+server.port, nil)
}

func (server *httpServer) status(w http.ResponseWriter, r *http.Request) {
	if server.ensureMethod(w, r, GET) {
		server.sendResponse(w, "Ready", "status")
	}
}

func (server *httpServer) insertMessage(w http.ResponseWriter, r *http.Request) {
	if server.ensureMethod(w, r, POST) {
		var message model.MessageJson
		json.Decode(&r.Body, &message)
		fmt.Println("Got message:", message)

		server.db.MessageTable.Insert(&message)
		server.db.AttachmentTable.InsertFromMessages(&message)
		server.db.ThreadParticipantTable.InsertFromMessages(&message)

		server.sendSuccess(w)

		server.websockets.broadcastMessages(&message)
	}
}

func (server *httpServer) insertMessages(w http.ResponseWriter, r *http.Request) {
	if server.ensureMethod(w, r, POST) {
		var messages []*model.MessageJson
		json.Decode(&r.Body, &messages)
		fmt.Println("Got messages:", messages)

		server.db.MessageTable.Insert(messages...)
		server.db.AttachmentTable.InsertFromMessages(messages...)
		server.db.ThreadParticipantTable.InsertFromMessages(messages...)

		server.sendSuccess(w)

		server.websockets.broadcastMessages(messages...)
	}
}

func (server *httpServer) updateContact(w http.ResponseWriter, r *http.Request) {
	if server.ensureMethod(w, r, POST) {
		var contact model.ContactJson
		json.Decode(&r.Body, &contact)
		fmt.Println("Got contact:", contact)

		server.db.ContactTable.Insert(&contact)
		server.db.ContactPhoneTable.DeleteFromContacts(&contact)
		server.db.ContactPhoneTable.InsertFromContacts(&contact)

		server.sendSuccess(w)

		server.websockets.broadcastContacts(&contact)
	}
}

func (server *httpServer) updateContacts(w http.ResponseWriter, r *http.Request) {
	if server.ensureMethod(w, r, POST) {
		var contacts []*model.ContactJson
		json.Decode(&r.Body, &contacts)
		fmt.Println("Got contacts:", contacts)

		server.db.ContactTable.Insert(contacts...)
		server.db.ContactPhoneTable.DeleteFromContacts(contacts...)
		server.db.ContactPhoneTable.InsertFromContacts(contacts...)

		server.sendSuccess(w)

		server.websockets.broadcastContacts(contacts...)
	}
}

func (server *httpServer) deleteContact(w http.ResponseWriter, r *http.Request) {
	if server.ensureMethod(w, r, DELETE) {
		var contact model.ContactJson
		json.Decode(&r.Body, &contact)
		fmt.Println("Deleting contact:", contact)

		server.db.ContactTable.Delete(&contact)
		server.db.ContactPhoneTable.DeleteFromContacts(&contact)

		server.sendSuccess(w)

		server.websockets.broadcastDeletedContacts(&contact)
	}
}

func (server *httpServer) deleteContacts(w http.ResponseWriter, r *http.Request) {
	if server.ensureMethod(w, r, DELETE) {
		var contacts []*model.ContactJson
		json.Decode(&r.Body, &contacts)
		fmt.Println("Deleting contacts:", contacts)

		server.db.ContactTable.Delete(contacts...)
		server.db.ContactPhoneTable.DeleteFromContacts(contacts...)

		server.sendSuccess(w)

		server.websockets.broadcastDeletedContacts(contacts...)
	}
}

////////////////////
// HELPER METHODS //
////////////////////

func (server *httpServer) sendResponse(w http.ResponseWriter, value interface{}, path ...string) {
	var response json.Json
	response.Set(value, path...)
	server.sendResponseJson(w, response)
}

func (server *httpServer) sendResponseJson(w http.ResponseWriter, response json.Json) {
	w.Header().Set("Content-Type", "application/json")
	fmt.Fprint(w, response.GetString())
}

func (server *httpServer) sendSuccess(w http.ResponseWriter) {
	server.sendResponse(w, "Success", "status")
}

func (server *httpServer) ensureMethod(w http.ResponseWriter, r *http.Request, method string) bool {
	if r.Method != method {
		server.sendBadMethod(w, method)
		return false
	}
	return true
}

func (server *httpServer) sendBadMethod(w http.ResponseWriter, method string) {
	server.sendError(w, "Endpoint requires "+method+" request.")
}

func (server *httpServer) sendError(w http.ResponseWriter, message string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadRequest)
	server.sendResponse(w, message, "error", "message")
}
