# ELK: Instalacion local (para pruebas)

#### Notas:

No hay autenticación ni ninguna config adicional, es solo un template de prueba para validar el estandar de los logs y que se visualizen como queremos en kibana.

#### Requisitos:

- Docker desktop (*)

#### Docker-compose:

Dentro de la carpeta docker-compose, ejecutamos:
```console
docker-compose up
```
De esta manera se levantará un elasticsearch, logstash y kibana (puertos 9200, 8089 y 5601 respectivamente)

- http://localhost:9200 (elastic)
- http://localhost:9600 (logstash)
- http://localhost:5160 (kibana)

#### Logstash config

- docker-compose/logstash/logstash.conf

#### Kibana config

- Para empezar a visualizar el contenido en el "Discover" de kibana, tenemos que crear el "Index Pattern":
- http://localhost:5601/app/kibana#/management/kibana/index_patterns/
- Ahi vamos a poder ubicar el o los indices según lo configurado en logstash.conf 
- Continuamos la configuracion hasta completarla y luego vamos a "Discover"
- http://localhost:5601/app/kibana#/discover y ahi vamos a poder el log generado

(*) Habilitar en los settings de docker desktop, el file sharing apuntando a la carpeta donde ejecutamos el proyecto