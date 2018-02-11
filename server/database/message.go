package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"github.com/osum4est/awesome-sms-server/model"
	"strings"
)

const (
	messageTableName = "message"

	messageColId       = "id"
	messageColDate     = "date"
	messageColProtocol = "protocol"
	messageColThreadId = "thread_id"
	messageColSender   = "sender"
	messageColBody     = "body"

	messageCreateTableSql = "CREATE TABLE IF NOT EXISTS " + messageTableName + " (" +
		messageColId + " integer NOT NULL," +
		messageColDate + " integer NOT NULL," +
		messageColProtocol + " integer NOT NULL," +
		messageColThreadId + " integer NOT NULL," +
		messageColSender + " text," + // Null if you sent
		messageColBody + " text," + // Message body can be null
		"UNIQUE(" + messageColId + "," + messageColProtocol + "));"
)

type messageTable struct {
	sqlDb *sql.DB
}

func (table *messageTable) createIfNotExists() {
	execOrThrow(table.sqlDb, messageCreateTableSql)
}

func (table *messageTable) Insert(messages ...*model.MessageJson) {
	stmt := "INSERT OR IGNORE INTO " + messageTableName + " VALUES"
	data := make([]interface{}, len(messages)*6) // Each message has 6 columns

	// Compile all messages into 1 query
	for i, message := range messages {
		// Get sender
		var sender string
		for _, address := range message.Addresses {
			if address.Type == model.MessageAddressTypeFrom {
				sender = address.Address
			}
		}

		// Add to stmt and data
		stmt += "(?,?,?,?,?,?),"
		data[i*6+0] = message.Id
		data[i*6+1] = message.Date
		data[i*6+2] = message.Protocol
		data[i*6+3] = message.ThreadId
		data[i*6+4] = sender
		data[i*6+5] = message.Body
	}
	stmt = strings.TrimRight(stmt, ",")

	// Insert messages into db
	_, err := table.sqlDb.Exec(stmt, data...)
	if err != nil {
		panic(err)
	}
}
