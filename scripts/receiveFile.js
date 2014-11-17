var http = require('http');
var fs = require('fs');

http.createServer(function (req, res) {
  var out = fs.createWriteStream('/tmp/deployment.zip');
  req.pipe(out);
  req.on('end', function () {
    res.writeHead(200);
    res.end('done');
  });
}).listen(8555, '127.0.0.1');
