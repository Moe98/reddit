set -m

/entrypoint.sh couchbase-server &

sleep 45

# Setup initial cluster/ Initialize Node
couchbase-cli cluster-init -c 127.0.0.1 --cluster-name $COUCHBASE_CLUSTER_NAME --cluster-username $COUCHBASE_ADMINISTRATOR_USERNAME \
--cluster-password $COUCHBASE_ADMINISTRATOR_PASSWORD --services data --cluster-ramsize $COUCHBASE_RAM --index-storage-setting default \

# Setup Administrator username and password
curl -v http://127.0.0.1:8091/settings/web -d port=8091 -d username=$COUCHBASE_ADMINISTRATOR_USERNAME -d password=$COUCHBASE_ADMINISTRATOR_PASSWORD

fg 1