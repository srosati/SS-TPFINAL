# Simulación de Sistemas Trabajo Practico Final

## Como ejecutar
- Instalar Java y Maven
- Crear archivo de configuración (Ver `config.txt` para un ejemplo)
- Ejecutar `mvn clean package`
- Ejecutar `./run.sh config.txt`

## Como hacer los graficos
- Crear un directorio con todas las configuraciones a analizar
  - El nombre de cada archivo deberia tener el formato `<nombre>.txt` (Ver directorios `amplitude_configs` y `frequency_configs` para ejemplos)
- Ejecutar `./run_dir.sh <directorio> <archivo_salida>`
- Para generar el gráfico de la curva de descarga, ejecutar `python3 graphics/plot_flow_curve.py <archivo_entrada> <archivo_salida> <paso_eje_y>`
  - El archivo de entrada es la salida del comando anterior
  - El paso del eje y es cada cuanto se muestra un label en el eje y
  - El archivo de salida es el nombre del archivo de salida (un png, jpg, etc)
- Para generar los graficos correspondientes a la ventana de caudal, asi como el caudal para cada variable ejectuar `python3 graphics/plot_delta_flow_n.py <archivo_entrada> <path_salida> <x_label>`
  - El archivo de entrada es la salida del comando anterior
  - El path de salida es el prefijo de los archivo de salida
    - Cada archivo de salida luego sera `<prefijo><variable>` o `<prefijo>flow` para el caudal
  - El x_label es el label del eje x para el grafico del caudal

## Como visualizar en ovito
- Instalar ovito
- Ejecutar el codigo con la configuración deseada
- Ejecutar `python3 graphics/ovito.py`
- Abrir el archivo `outFiles/final.ovito` en ovito
- Cargar los datos de `outFiles/ovito.txt` (ver preset Pildora para el orden de los mismos)