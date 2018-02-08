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
	attachmentColDataPath  = "data_path"

	attachmentCreateTableSql = "CREATE TABLE IF NOT EXISTS " + attachmentTableName + " (" +
		attachmentColId + " integer PRIMARY KEY," +
		attachmentColMessageId + " integer NOT NULL," +
		attachmentColMime + " text NOT NULL," +
		attachmentColDataPath + " text NOT NULL" +
		");"
)

type attachmentTable struct {
	sqlDb *sql.DB
}

func (table *attachmentTable) createIfNotExists() {
	execOrThrow(table.sqlDb, attachmentCreateTableSql)
}
