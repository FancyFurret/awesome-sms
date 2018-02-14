package main

import (
	"fmt"
	"github.com/osum4est/awesome-sms-server/database"
	"github.com/osum4est/awesome-sms-server/network"
)

const (
	port = "11150"
)

var db *database.DB

func main() {

	db = &database.DB{}
	db.Open()

	fmt.Println("Starting AwesomeSMS http server on port", port)
	httpServer := network.NewHttpServer(port, db)
	httpServer.Start()
}

