@REM @REM RabbitMQ
@REM kubectl apply -f ./rabbitmq/rabbitmq-operator-deployment.yaml
@REM @REM kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml
@REM @REM kubectl annotate storageclass local-path storageclass.kubernetes.io/is-default-class=true

@REM @REM ArangoDB
@REM kubectl apply -f ./arangodb/arango-crd.yaml
@REM kubectl apply -f ./arangodb/arango-deployment.yaml
@REM kubectl apply -f ./arangodb/arango-storage.yaml
@REM kubectl apply -f ./arangodb/arango-deployment-replication.yaml

@REM @REM Couchbase
@REM kubectl apply -f ./couchbase/couchbase-crd.yaml
@REM couchbase\bin\cbopcfg create admission
@REM couchbase\bin\cbopcfg create operator

@REM @REM Cassandra
@REM kubectl apply -f ./cassandra/cassandra-operator.yaml
@REM kubectl apply -f ./cassandra/cassandra-storage.yaml

PAUSE