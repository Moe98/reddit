@REM @REM RabbitMQ
@REM kubectl apply -f ./rabbitmq/rabbitmq-deployment.yaml

@REM @REM ArangoDB
@REM kubectl apply -f ./arangodb/arangodb-deployment.yaml

@REM @REM Couchbase
@REM kubectl apply -f ./couchbase/couchbase-deployment.yaml

@REM @REM Postgres
@REM kubectl create configmap sql-scripts --from-file=../libs/postgres/src/main/resources/sql
@REM kubectl apply -f ./postgres/postgres-deployment.yaml

@REM @REM Cassandra
@REM kubectl -n cass-operator apply -f ./cassandra/cassandra-deployment.yaml

PAUSE