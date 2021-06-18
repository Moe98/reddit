# arangosh --server.endpoint tcp://127.0.0.1:8529 ...
# ...
#!/bin/bash
/usr/bin/arangosh \
--server.endpoint tcp://0.0.0.0:8529
--server.password ${ARANGO_ROOT_PASSWORD} \
--javascript.execute-string "db._createDatabase(\"Reddit\");db._create(\"Users\");"
# db._createDatabase("Reddit");
# db._create("Users");