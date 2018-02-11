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

func (table *contactPhoneTable) Insert(contact_id int, number string, number_type int) error {
	_, err := table.sqlDb.Exec("INSERT INTO "+contactPhoneTableName+" VALUES(?, ?, ?);",
		contact_id, number, number_type)
	if err != nil {
		panic(err)
	}
	return nil
}

func (table *contactPhoneTable) Delete(contact_id int) error {
	_, err := table.sqlDb.Exec("DELETE FROM "+contactPhoneTableName+" WHERE "+
		contactPhoneColContactId+"=?;", contact_id)
	if err != nil {
		panic(err)
	}
	return nil
}
