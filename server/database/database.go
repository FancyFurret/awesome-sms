package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"log"
)

// Currently unused, may be used in future
type ITable interface {
	createIfNotExists()
}

const (
	databaseName = "AwesomeSMS.db"
)

type DB struct {
	sqlDb           *sql.DB

	// Tables
	messageTable    *messageTable
	attachmentTable *attachmentTable
	threadParticipantTable *threadParticipantTable
	contactTable *contactTable
	contactPhoneTable *contactPhoneTable
}

func (db *DB) Open() {
	// Open the database
	var err error
	db.sqlDb, err = sql.Open("sqlite3", "./"+databaseName)
	if err != nil {
		log.Fatal(err)
	}

	// Initialize tables with the database
	db.messageTable = &messageTable{db.sqlDb}
	db.attachmentTable = &attachmentTable{db.sqlDb}
	db.threadParticipantTable = &threadParticipantTable{db.sqlDb}
	db.contactTable = &contactTable{db.sqlDb}
	db.contactPhoneTable = &contactPhoneTable{db.sqlDb}

	// Create the tables if they don't already exist
	db.messageTable.createIfNotExists()
	db.attachmentTable.createIfNotExists()
	db.threadParticipantTable.createIfNotExists()
	db.contactTable.createIfNotExists()
	db.contactPhoneTable.createIfNotExists()
}
