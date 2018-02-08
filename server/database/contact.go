package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
)

const (
	contactTableName = "contact"

	contactColId            = "id"
	contactColName          = "name"
	contactColThumbnailPath = "thumbnail_path"

	contactCreateTableSql = "CREATE TABLE IF NOT EXISTS " + contactTableName + " (" +
		contactColId + " integer PRIMARY KEY," +
		contactColName + " text," + // Can have numbers without names or photos
		contactColThumbnailPath + " text" +
		");"
)

type contactTable struct {
	sqlDb *sql.DB
}

func (table *contactTable) createIfNotExists() {
	execOrThrow(table.sqlDb, contactCreateTableSql)
}
