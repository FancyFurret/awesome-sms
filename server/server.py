#!/usr/bin/python3

from http.server import BaseHTTPRequestHandler, HTTPServer
import json


HOSTNAME = ""
PORT = 11200


class Server(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        raw_data = self.rfile.read(content_length)
        data = json.loads(raw_data)

        if (self.path == "/store"):
            

        self.send_response(200)


if __name__ == "__main__":
    print("Starting AwesomeSMS server on port " + str(PORT))

    s = HTTPServer((HOSTNAME, PORT), Server)
    s.serve_forever()
