package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"strings"
)

const (
	threadParticipantTableName = "thread_participant"

	threadParticipantColThreadId = "thread_id"
	threadParticipantColPhone    = "phone"

	threadParticipantCreateTableSql = "CREATE TABLE IF NOT EXISTS " + threadParticipantTableName + " (" +
		threadParticipantColThreadId + " integer NOT NULL," +
		threadParticipantColPhone + " text NOT NULL" +
		");"
)

type threadParticipantTable struct {
	sqlDb *sql.DB
}

func (table *threadParticipantTable) createIfNotExists() {
	execOrThrow(table.sqlDb, threadParticipantCreateTableSql)
}

func (table *threadParticipantTable) InsertIfNotExists(thread_id int, addresses []string) error {
	// See if thread already exists
	rowErr := table.sqlDb.QueryRow("SELECT * FROM "+threadParticipantTableName+
		" WHERE "+threadParticipantColThreadId+"=?;", thread_id).Scan()
	if rowErr != sql.ErrNoRows {
		return nil
	}

	// Make statement
	stmt := "INSERT INTO " + threadParticipantTableName + " VALUES"
	stmt += strings.Repeat("(?, ?),", len(addresses))
	stmt = strings.TrimRight(stmt, ",")

	// Make data
	data := make([]interface{}, 0)
	for _, address := range addresses {
		data = append(data, thread_id, address)
	}

	// Execute sql
	_, err := table.sqlDb.Exec(stmt, data...)
	if err != nil {
		panic(err)
	}
	return nil
}
