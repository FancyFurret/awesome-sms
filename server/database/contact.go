package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
)

const (
	contactTableName = "contact"

	contactColId        = "id"
	contactColName      = "name"
	contactColThumbnail = "thumbnail"

	contactCreateTableSql = "CREATE TABLE IF NOT EXISTS " + contactTableName + " (" +
		contactColId + " integer PRIMARY KEY," +
		contactColName + " text NOT NULL," +
		contactColThumbnail + " blob NOT NULL" +
		");"
)

type contactTable struct {
	sqlDb *sql.DB
}

func (table *contactTable) createIfNotExists() {
	execOrThrow(table.sqlDb, contactCreateTableSql)
}

func (table *contactTable) Insert(id int, name string, thumbnail []byte) error {
	_, err :=
		table.sqlDb.Exec("INSERT OR REPLACE INTO "+contactTableName+" VALUES(?, ?, ?);",
			id, name, thumbnail)
	if err != nil {
		panic(err)
	}
	return nil
}

func (table *contactTable) Delete(id int) error {
	_, err :=
		table.sqlDb.Exec("DELETE FROM "+contactTableName+" WHERE "+contactColId+"=?;",
			id)
	if err != nil {
		panic(err)
	}
	return nil
}
