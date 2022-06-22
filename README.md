# Procesador
Autores: 
Rafael Alonso Sirera
Idir Carlos Aliane Crespo
Juan Castro Rodriguez

Uso:
java -jar procesador.java <input.txt>

Información de uso:
Para ejecutar el procesador, nos debemos situar en la raiz del proyecto, 
donde se encuentra el archivo procesador.jar 
y a continuación ejecutar el mandato descrito en el apartado Uso.

Estructura de carpetas:
./
    bin/                ficheros binarios de la compilación
    data/               inputs correctos y con errores
    doc/                documentación (definición de tokens, gramáticas, acciones semánticas...)
    src/                ficheros fuente del procesador
    actiongototable.csv tabla acción y goto del ASin ascendente que leerá el programa
    procesador.jar      fichero ejecutable
    README.md           este fichero

Una vez ejecutado el procesador, se generarán los ficheros tokens.txt, parse.txt, TS.txt y errores.txt en la raiz.
Si no aparecen, refrescar la carpeta.