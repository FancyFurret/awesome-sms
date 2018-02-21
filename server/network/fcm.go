package network

import (
	"github.com/NaySoftware/go-fcm"
	//"github.com/osum4est/awesome-sms-server/json"
)

type fcmClient struct {
	client *fcm.FcmClient
}

func newFcmClient() *fcmClient {
	fcmClient := &fcmClient{}
	fcmClient.client = fcm.NewFcmClient(fcmServerKey)

	return fcmClient
}

func (fcmClient *fcmClient) sendMessage(jsonBody map[string]interface{}) {
	fcmClient.client.NewFcmMsgTo(myPhoneKey, jsonBody)

	status, err := fcmClient.client.Send()
	if err != nil {
		panic(err)
	}
	status.PrintResults()
}
