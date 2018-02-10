package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
)

const (
	attachmentTableName = "attachment"

	attachmentColId        = "id"
	attachmentColMessageId = "message_id"
	attachmentColMime      = "mime"
	attachmentColData      = "data"

	attachmentCreateTableSql = "CREATE TABLE IF NOT EXISTS " + attachmentTableName + " (" +
		attachmentColId + " integer PRIMARY KEY AUTOINCREMENT," +
		attachmentColMessageId + " integer NOT NULL," +
		attachmentColMime + " text NOT NULL," +
		attachmentColData + " blob NOT NULL" +
		");"
)

type attachmentTable struct {
	sqlDb *sql.DB
}

func (table *attachmentTable) createIfNotExists() {
	execOrThrow(table.sqlDb, attachmentCreateTableSql)
}

func (table *attachmentTable) Insert(message_id int, mime string, data []byte) error {
	_, err := table.sqlDb.Exec("INSERT INTO "+attachmentTableName+" ("+
		attachmentColMessageId+","+attachmentColMime+","+attachmentColData+
		") VALUES(?, ?, ?);",
		message_id, mime, data)
	if err != nil {
		panic(err)
	}
	return nil
}
