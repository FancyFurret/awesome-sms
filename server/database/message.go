package database

import (
	"database/sql"
	"errors"
	_ "github.com/mattn/go-sqlite3"
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
		messageColId + " integer PRIMARY KEY," +
		messageColDate + " integer NOT NULL," +
		messageColProtocol + " integer NOT NULL," +
		messageColThreadId + " integer NOT NULL," +
		messageColSender + " text," + // Null if you sent
		messageColBody + " text" + // Message body can be null
		");"
)

type messageTable struct {
	sqlDb *sql.DB
}

func (table *messageTable) createIfNotExists() {
	execOrThrow(table.sqlDb, messageCreateTableSql)
}

func (table *messageTable) Insert(id int, date int64, protocol byte, threadId int, sender string, body string) error {
	_, err := table.sqlDb.Exec("INSERT INTO "+messageTableName+" VALUES(?, ?, ?, ?, ?, ?);",
		id, date, protocol, threadId, sender, body)
	if isErrorUniqueConstraintFailed(err) {
		return errors.New("Message with that id already exists!")
	} else if err != nil {
		panic(err)
	}
	return nil
}
