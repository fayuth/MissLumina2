#!/usr/bin/env bash
# Arranca MissLumina (Spring Boot) en segundo plano.
# Carga las variables de entorno desde el archivo .env si existe.
set -e

cd "$(dirname "$0")"

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi

: "${DB_URL:=jdbc:postgresql://127.0.0.1:5432/miss_lumina_db}"
: "${DB_USERNAME:=postgres}"
: "${DB_PASSWORD:=postgres}"
export DB_URL DB_USERNAME DB_PASSWORD

JAR="$(ls target/miss-lumina-*.jar 2>/dev/null | head -n 1)"
if [ -z "$JAR" ]; then
  echo "ERROR: no se encontro el .jar en target/."
  echo "Compila primero con:  ./mvnw clean package -DskipTests"
  exit 1
fi

# Detiene una instancia previa si existe
pkill -f 'java -jar.*miss-lumina' 2>/dev/null || true
sleep 2

nohup java -jar "$JAR" > app.log 2>&1 &
echo "App iniciada (PID $!). Logs: $(pwd)/app.log"
echo "Revisa el arranque con:  tail -f app.log"
