package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
)

const (
	threadParticipantTableName = "thread_participant"

	threadParticipantColThreadId  = "thread_id"
	threadParticipantColContactId = "contact_id"

	threadParticipantCreateTableSql = "CREATE TABLE IF NOT EXISTS " + threadParticipantTableName + " (" +
		threadParticipantColThreadId + " integer NOT NULL," +
		threadParticipantColContactId + " integer NOT NULL" +
		");"
)

type threadParticipantTable struct {
	sqlDb *sql.DB
}

func (table *threadParticipantTable) createIfNotExists() {
	execOrThrow(table.sqlDb, threadParticipantCreateTableSql)
}
