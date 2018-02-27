package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"github.com/osum4est/awesome-sms-server/model"
	"strings"
	"encoding/base64"
)

const (
	attachmentTableName = "attachment"

	attachmentColId        = "id"
	attachmentColMessageId = "message_id"
	attachmentColMime      = "mime"
	attachmentColData      = "data"

	attachmentCreateTableSql = "CREATE TABLE IF NOT EXISTS " + attachmentTableName + " (" +
		attachmentColId + " integer PRIMARY KEY," +
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

func (table *attachmentTable) InsertFromMessages(messages ...*model.MessageJson) {
	stmt := "INSERT OR IGNORE INTO " + attachmentTableName + " VALUES"
	data := make([]interface{}, 0)

	// Compile all attachments into 1 query
	for _, message := range messages {
		for _, attachment := range message.Attachments {

			byteData, err := base64.StdEncoding.DecodeString(attachment.Data)
			if err != nil {
				panic(err)
			}

			// Add to stmt and data
			stmt += "(?,?,?,?),"
			data = append(
				data,
				attachment.Id,
				message.Id,
				attachment.Mime,
				byteData)
		}
	}
	stmt = strings.TrimRight(stmt, ",")

	// Insert attachments into db
	if len(data) > 0 {
		_, err := table.sqlDb.Exec(stmt, data...)
		if err != nil {
			panic(err)
		}
	}
}
