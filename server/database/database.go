package database

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"log"
	"strings"
)

// Currently unused, may be used in future
type ITable interface {
	createIfNotExists()
}

const (
	databaseName = "AwesomeSMS.db"
)

type DB struct {
	sqlDb *sql.DB

	// Tables
	MessageTable           *messageTable
	AttachmentTable        *attachmentTable
	ThreadParticipantTable *threadParticipantTable
	ContactTable           *contactTable
	ContactPhoneTable      *contactPhoneTable
}

func (db *DB) Open() {
	// Open the database
	var err error
	db.sqlDb, err = sql.Open("sqlite3", "./"+databaseName)
	if err != nil {
		log.Fatal(err)
	}

	// Initialize tables with the database
	db.MessageTable = &messageTable{db.sqlDb}
	db.AttachmentTable = &attachmentTable{db.sqlDb}
	db.ThreadParticipantTable = &threadParticipantTable{db.sqlDb}
	db.ContactTable = &contactTable{db.sqlDb}
	db.ContactPhoneTable = &contactPhoneTable{db.sqlDb}

	// Create the tables if they don't already exist
	db.MessageTable.createIfNotExists()
	db.AttachmentTable.createIfNotExists()
	db.ThreadParticipantTable.createIfNotExists()
	db.ContactTable.createIfNotExists()
	db.ContactPhoneTable.createIfNotExists()
}

func isErrorUniqueConstraintFailed(err error) bool {
	return err != nil && strings.Contains(err.Error(), "UNIQUE constraint failed")
}
