@REM @REM RabbitMQ
@REM kubectl delete -f ./rabbitmq/rabbitmq-operator-deployment.yaml

@REM @REM ArangoDB
@REM kubectl delete -f ./arangodb/arango-crd.yaml
@REM kubectl delete -f ./arangodb/arango-deployment.yaml
@REM kubectl delete -f ./arangodb/arango-storage.yaml
@REM kubectl delete -f ./arangodb/arango-deployment-replication.yaml

@REM @REM Couchbase
@REM kubectl delete -f ./couchbase/couchbase-crd.yaml
@REM couchbase\bin\cbopcfg delete admission
@REM couchbase\bin\cbopcfg delete operator

@REM @REM Cassandra
@REM kubectl delete -f ./cassandra/cassandra-operator.yaml
@REM kubectl delete -f ./cassandra/cassandra-storage.yaml

PAUSE