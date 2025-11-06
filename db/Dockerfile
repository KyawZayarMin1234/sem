# db/Dockerfile
FROM mysql:8.0.32

# Root + default DB + app user (used by your app container)
ENV MYSQL_ROOT_PASSWORD=example \
    MYSQL_DATABASE=employees \
    MYSQL_USER=employees \
    MYSQL_PASSWORD=example

# Bring the full dataset (keep directory layout intact)
COPY test_db /docker-entrypoint-initdb.d/test_db

# Load the dataset on first boot (runs once when datadir is empty)
RUN printf '%s\n' '#!/bin/sh' \
    'set -e' \
    'echo "[init] Loading employees.sql into database: $MYSQL_DATABASE"' \
    'cd /docker-entrypoint-initdb.d/test_db' \
    'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" < employees.sql' \
    > /docker-entrypoint-initdb.d/01-load-employees.sh \
 && chmod +x /docker-entrypoint-initdb.d/01-load-employees.sh
