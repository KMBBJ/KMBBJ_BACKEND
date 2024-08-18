CREATE KEYSPACE IF NOT EXISTS ${CASSANDRA_KEYSPACE}
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}
     AND durable_writes = true;