package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"github.com/osum4est/awesome-sms-server/model"
	"strings"
)

const (
	contactPhoneTableName = "contact_phone"

	contactPhoneColContactId = "contact_id"
	contactPhoneColNumber    = "number"
	contactPhoneColType      = "type"

	contactPhoneCreateTableSql = "CREATE TABLE IF NOT EXISTS " + contactPhoneTableName + " (" +
		contactPhoneColContactId + " integer NOT NULL," +
		contactPhoneColNumber + " text NOT NULL," +
		contactPhoneColType + " integer NOT NULL" +
		");"
)

type contactPhoneTable struct {
	sqlDb *sql.DB
}

func (table *contactPhoneTable) createIfNotExists() {
	execOrThrow(table.sqlDb, contactPhoneCreateTableSql)
}

func (table *contactPhoneTable) InsertFromContacts(contacts ...*model.ContactJson) {
	stmt := "INSERT OR IGNORE INTO " + contactPhoneTableName + " VALUES"
	data := make([]interface{}, 0)

	// Compile all phones into 1 query
	for _, contact := range contacts {
		for _, phone := range contact.Phones {
			// Add to stmt and data
			stmt += "(?,?,?),"
			data = append(data, contact.Id, phone.Number, phone.Type)
		}
	}
	stmt = strings.TrimRight(stmt, ",")

	// Execute sql
	_, err := table.sqlDb.Exec(stmt, data...)
	if err != nil {
		panic(err)
	}
}

func (table *contactPhoneTable) DeleteFromContacts(contacts ...*model.ContactJson) {
	stmt := "DELETE FROM " + contactPhoneTableName +
		" WHERE " + contactPhoneColContactId + " IN("
	data := make([]interface{}, len(contacts))

	// Compile all phones into 1 query
	for i, contact := range contacts {
		stmt += "?,"
		data[i] = contact.Id
	}
	stmt = strings.TrimRight(stmt, ",")
	stmt = stmt + ")"

	// Delete phones from db
	_, err := table.sqlDb.Exec(stmt, data...)
	if err != nil {
		panic(err)
	}
}
