var express = require('express');
var cors = require('cors');

var app = express();

// Enable CORS for all requests
app.use(cors());

// allow serving of static files from the public directory
app.use(express.static(__dirname));

var port = 9090;
var host = '0.0.0.0';
var server = app.listen(port, host, function() {
  console.log("App started at: " + new Date() + " on port: " + port); 
});
