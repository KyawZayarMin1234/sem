# db/Dockerfile
FROM mysql:8.0.32

# Root password and default DB name (lab expects 'employees')
ENV MYSQL_ROOT_PASSWORD=example
ENV MYSQL_DATABASE=employees

# Copy the whole test_db directory (your layout stays the same)
# Build context here is the db/ folder, so this path is correct.
COPY test_db /docker-entrypoint-initdb.d/test_db

# db/Dockerfile (only this part changes)
RUN printf '%s\n' '#!/bin/sh' \
    'set -e' \
    'echo "[init] Loading employees.sql into database: $MYSQL_DATABASE"' \
    'cd /docker-entrypoint-initdb.d/test_db' \
    'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" < employees.sql' \
    > /docker-entrypoint-initdb.d/01-load-employees.sh \
 && chmod +x /docker-entrypoint-initdb.d/01-load-employees.sh
