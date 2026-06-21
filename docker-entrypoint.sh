#!/bin/sh
if [ ! -f /app/config/aes-key.txt ]; then
    openssl rand -base64 32 > /app/config/aes-key.txt
    echo "Generated new AES-256 encryption key"
fi
exec java -jar app.jar
