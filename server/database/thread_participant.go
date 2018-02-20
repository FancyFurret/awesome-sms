package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"github.com/osum4est/awesome-sms-server/model"
	"strings"
)

const (
	threadParticipantTableName = "thread_participant"

	threadParticipantColThreadId = "thread_id"
	threadParticipantColPhone    = "phone"

	threadParticipantCreateTableSql = "CREATE TABLE IF NOT EXISTS " + threadParticipantTableName + " (" +
		threadParticipantColThreadId + " integer NOT NULL," +
		threadParticipantColPhone + " text NOT NULL," +
		"UNIQUE(" + threadParticipantColThreadId + "," + threadParticipantColPhone + ")" +
		");"
)

type threadParticipantTable struct {
	sqlDb *sql.DB
}

func (table *threadParticipantTable) createIfNotExists() {
	execOrThrow(table.sqlDb, threadParticipantCreateTableSql)
}

func (table *threadParticipantTable) InsertFromMessages(messages ...*model.MessageJson) {
	stmt := "INSERT OR IGNORE INTO " + threadParticipantTableName + " VALUES"
	data := make([]interface{}, 0)

	// Compile all thread participants into 1 query
	for _, message := range messages {
		addresses := make([]string, 0)
		for _, address := range message.Addresses {
			if !contains(addresses, address.Address) {
				addresses = append(addresses, address.Address)
				// Add to stmt and data
				stmt += "(?, ?),"
				data = append(data, message.ThreadId, address.Address)
			}
		}
	}
	stmt = strings.TrimRight(stmt, ",")

	// Execute sql
	_, err := table.sqlDb.Exec(stmt, data...)
	if err != nil {
		panic(err)
	}
}
