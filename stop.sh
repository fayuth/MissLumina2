#!/usr/bin/env bash
# Detiene MissLumina.
if pkill -f 'java -jar.*miss-lumina'; then
  echo "App detenida."
else
  echo "La app no estaba corriendo."
fi
