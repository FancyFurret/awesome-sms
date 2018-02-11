package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"github.com/osum4est/awesome-sms-server/model"
	"strings"
)

const (
	contactTableName = "contact"

	contactColId        = "id"
	contactColName      = "name"
	contactColThumbnail = "thumbnail"

	contactCreateTableSql = "CREATE TABLE IF NOT EXISTS " + contactTableName + " (" +
		contactColId + " integer PRIMARY KEY," +
		contactColName + " text NOT NULL," +
		contactColThumbnail + " blob" +
		");"
)

type contactTable struct {
	sqlDb *sql.DB
}

func (table *contactTable) createIfNotExists() {
	execOrThrow(table.sqlDb, contactCreateTableSql)
}

func (table *contactTable) Insert(contacts ...*model.ContactJson) {
	stmt := "INSERT OR REPLACE INTO " + contactTableName + " VALUES"
	data := make([]interface{}, len(contacts)*3) // Each contact has 3 columns

	// Compile all contacts into 1 query
	for i, contact := range contacts {
		stmt += "(?,?,?),"
		data[i*3+0] = contact.Id
		data[i*3+1] = contact.Name
		data[i*3+2] = contact.Thumbnail
	}
	stmt = strings.TrimRight(stmt, ",")

	// Insert contacts into db
	_, err := table.sqlDb.Exec(stmt, data...)
	if err != nil {
		panic(err)
	}
}

func (table *contactTable) Delete(contacts ...*model.ContactJson) {
	stmt := "DELETE FROM " + contactTableName + " WHERE " + contactColId + " IN("
	data := make([]interface{}, len(contacts))

	// Compile all contacts into 1 query
	for i, contact := range contacts {
		stmt += "?,"
		data[i] = contact.Id
	}
	stmt = strings.TrimRight(stmt, ",")
	stmt = stmt + ")"

	// Delete contacts from db
	_, err := table.sqlDb.Exec(stmt, data...)
	if err != nil {
		panic(err)
	}
}
