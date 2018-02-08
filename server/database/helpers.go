package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"log"
)

func execOrThrow(db *sql.DB, sql string) {
	_, err := db.Exec(sql)
	if err != nil {
		log.Fatal(err)
	}
}
