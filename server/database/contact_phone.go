package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
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
