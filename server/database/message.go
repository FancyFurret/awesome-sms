package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
)

const (
	messageTableName = "message"

	messageColId       = "id"
	messageColDate     = "date"
	messageColProtocol = "protocol"
	messageColThreadId = "thread_id"
	messageColSenderId = "sender"
	messageColBody     = "body"

	messageCreateTableSql = "CREATE TABLE IF NOT EXISTS " + messageTableName + " (" +
		messageColId + " integer PRIMARY KEY," +
		messageColDate + " integer NOT NULL," +
		messageColProtocol + " integer NOT NULL," +
		messageColThreadId + " integer NOT NULL," +
		messageColSenderId + " integer NOT NULL," +
		messageColBody + " text" + // Message body can be null
		");"
)

type messageTable struct {
	sqlDb *sql.DB
}

func (table *messageTable) createIfNotExists() {
	execOrThrow(table.sqlDb, messageCreateTableSql)
}
