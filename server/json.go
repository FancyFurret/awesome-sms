package main

import (
	encjson "encoding/json"
	//	"fmt"
	"io"
)

type json struct {
	data map[string]interface{}
}

func (json *json) Set(value interface{}, path ...string) {
	// Lazy initialization of data
	if json.data == nil {
		json.data = make(map[string]interface{})
	}

	// Loop through all path items and create necessary maps
	previous := json.data
	for _, pathItem := range path[:len(path)-1] {
		// Make the new map if it doesn't exist
		if _, ok := previous[pathItem]; !ok {
			previous[pathItem] = make(map[string]interface{})
		}

		// Move forwards down the chain
		previous = previous[pathItem].(map[string]interface{})
	}

	// Set the data
	previous[path[len(path)-1]] = value
}

func (json *json) GetString() string {
	val, _ := encjson.Marshal(json.data)
	return string(val)
}

func Decode(r *io.ReadCloser, object interface{}) {
	decoder := encjson.NewDecoder(*r)
	err := decoder.Decode(object)
	if err != nil {
		panic(err)
	}
	(*r).Close()
}
