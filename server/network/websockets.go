package network

import (
	"github.com/gorilla/websocket"
	"github.com/osum4est/awesome-sms-server/database"
	"net/http"
	"github.com/osum4est/awesome-sms-server/model"
	"fmt"
)

type websocketServer struct {
	db       *database.DB
	upgrader *websocket.Upgrader
	fcm      *fcmClient

	// TODO: Allow for multiple users on one server
	sockets []*websocket.Conn

	// TODO: Store these somewhere else
	currentPendingAttachmentsId int
	pendingAttachments          map[int]*model.MessageAttachmentJson
}

func NewWebSocketServer(db *database.DB) *websocketServer {
	return &websocketServer{db, &websocket.Upgrader{
		ReadBufferSize:  1024,
		WriteBufferSize: 1024,
		CheckOrigin: func(r *http.Request) bool {
			return true
		}},
		newFcmClient(),
		make([]*websocket.Conn, 0),
		0,
		make(map[int]*model.MessageAttachmentJson, 0)}
}

func (server *websocketServer) newConnection(w http.ResponseWriter, r *http.Request) {
	// Upgrade initial GET request to a websocket
	ws, err := server.upgrader.Upgrade(w, r, nil)
	if err != nil {
		panic(err)
	}

	// Make sure we close the connection when the function returns
	defer ws.Close()

	// Add to our list of sockets
	server.sockets = append(server.sockets, ws)

	for {
		message := make([]interface{}, 0)
		err := ws.ReadJSON(&message)
		if err != nil {
			if _, ok := err.(*websocket.CloseError); ok {
				// Remove socket from slice
				for i, socket := range server.sockets {
					if ws == socket {
						server.sockets = append(server.sockets[:i], server.sockets[i+1:]...)
					}
				}

				fmt.Println(ws.RemoteAddr().String() + " has disconnected")
				return
			} else {
				panic(err) // TODO: Send bad request error
			}
		}

		json := message[1].(map[string]interface{})
		switch message[0] {
		case "get_new_messages":
			server.getNewMessages(json, ws)
		case "get_threads":
			server.getThreads(json, ws)
		case "get_contacts":
			server.getContacts(json, ws)
		case "send_message":
			server.sendMessage(json, ws)
		}
	}
}

func (server *websocketServer) broadcastMessages(messages ...*model.MessageJson) {
	for _, ws := range server.sockets {
		ws.WriteJSON([2]interface{}{"new_messages", messages})
	}
}

func (server *websocketServer) broadcastContacts(contacts ...*model.ContactJson) {
	for _, ws := range server.sockets {
		ws.WriteJSON([2]interface{}{"new_contacts", contacts})
	}
}

func (server *websocketServer) broadcastDeletedContacts(contacts ...*model.ContactJson) {
	for _, ws := range server.sockets {
		ws.WriteJSON([2]interface{}{"deleted_contacts", contacts})
	}
}

func (server *websocketServer) getNewMessages(json map[string]interface{}, ws *websocket.Conn) {
	lastDateReceived := int64(json["lastDateReceived"].(float64))
	amount := int(json["amount"].(float64))
	messages := server.db.MessageTable.GetNewMessages(lastDateReceived, amount)
	ws.WriteJSON([2]interface{}{"new_messages", messages})
}

func (server *websocketServer) getThreads(json map[string]interface{}, ws *websocket.Conn) {
	// TODO: Rename because amount is misleading
	amount := int(json["amount"].(float64))
	messages := server.db.MessageTable.GetThreads(amount)
	ws.WriteJSON([2]interface{}{"new_messages", messages})
}

func (server *websocketServer) getContacts(json map[string]interface{}, ws *websocket.Conn) {
	contacts := server.db.ContactTable.GetContacts()
	ws.WriteJSON([2]interface{}{"new_contacts", contacts})
}

func (server *websocketServer) sendMessage(json map[string]interface{}, ws *websocket.Conn) {
	// Save attachments so they can be accessed with a future http request, since they are
	// too big to send over fcm
	attachments := json["attachments"].([]interface{})
	jsonAttachments := make([]int, len(attachments))
	json["attachments"] = jsonAttachments
	for i, attachment := range attachments {
		attachmentMap := attachment.(map[string]interface{})
		pendingAttachment :=
			&model.MessageAttachmentJson{
				Id:   -1,
				Mime: attachmentMap["mime"].(string),
				Data: attachmentMap["data"].(string)}
		server.pendingAttachments[server.currentPendingAttachmentsId] = pendingAttachment
		jsonAttachments[i] = server.currentPendingAttachmentsId

		server.currentPendingAttachmentsId++
	}

	json["event"] = "send_message"
	server.fcm.sendMessage(json)
}
