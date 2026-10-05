#!/usr/bin/env bash
# Prueba de caracterización: compara la salida actual con salida_original.txt
# ignorando la fecha/hora de la línea de auditoría.
cd "$(dirname "$0")"
./run.sh > salida_nueva.txt
mask() { sed -E 's/[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:.]+/<FECHA>/' "$1"; }
if diff <(mask salida_original.txt) <(mask salida_nueva.txt); then
  echo "OK: el comportamiento no cambió"
else
  echo "FALLO: la salida cambió" ; exit 1
fi
