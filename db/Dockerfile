FROM mysql:8.0.32

ENV MYSQL_ROOT_PASSWORD=example \
    MYSQL_DATABASE=employees \
    MYSQL_USER=app \
    MYSQL_PASSWORD=example

# copies db/test_db/** into the image
COPY test_db /docker-entrypoint-initdb.d/test_db

RUN printf '%s\n' '#!/bin/sh' \
    'set -e' \
    'echo "[init] Loading employees.sql into database: $MYSQL_DATABASE"' \
    'cd /docker-entrypoint-initdb.d/test_db' \
    'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" < employees.sql' \
    > /docker-entrypoint-initdb.d/01-load-employees.sh \
 && chmod +x /docker-entrypoint-initdb.d/01-load-employees.sh
