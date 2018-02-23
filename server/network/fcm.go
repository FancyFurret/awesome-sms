package network

import (
	"github.com/NaySoftware/go-fcm"
)

type fcmClient struct {
	client *fcm.FcmClient
}

func newFcmClient() *fcmClient {
	fcmClient := &fcmClient{}
	fcmClient.client = fcm.NewFcmClient(fcmServerKey)

	return fcmClient
}

func (fcmClient *fcmClient) sendMessage(json map[string]interface{}) {
	fcmClient.client.NewFcmMsgTo(myPhoneKey, json)

	status, err := fcmClient.client.Send()
	if err != nil {
		panic(err)
	}
	status.PrintResults()
}
