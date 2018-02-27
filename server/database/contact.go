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

	contactGetContactsSql = `
SELECT
contact.id,
contact.name,
contact.thumbnail,
contact_phone.number,
contact_phone.type
FROM contact
  JOIN contact_phone ON contact.id = contact_phone.contact_id;

`
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

func (table *contactTable) GetContacts() *[]model.ContactJson {
	rows, err := table.sqlDb.Query(contactGetContactsSql)
	if err != nil {
		panic(err)
	}
	defer rows.Close()

	// TODO: Thumbnails take a while to send. Send over url instead?
	contacts := make([]model.ContactJson, 0)
	lastId := -1

	for rows.Next() {
		contact := &model.ContactJson{}

		var (
			number     string
			numberType int
		)

		err := rows.Scan(
			&contact.Id,
			&contact.Name,
			&contact.Thumbnail,
			&number,
			&numberType)
		if err != nil {
			panic(err)
		}

		// Get the already created contact if this contact has already been created
		if lastId == contact.Id {
			contact = &contacts[len(contacts)-1]
		}

		// Add phone
		contact.Phones = append(contact.Phones,
			model.ContactPhoneJson{
				Number: number,
				Type:   numberType})

		// Add the contact to the array if it hasn't been added yet
		if lastId != contact.Id {
			contacts = append(contacts, *contact)
		}

		lastId = contact.Id
	}

	return &contacts
}
