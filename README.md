# Logging Kotlin Template   

## Modulos:

- com.demo.logging: Contiene el core de la lib. Se recomienda usarla cuando nuestra app NO tenga Spring. El motivo es que van a tener que completar todos los logs de forma MANUAL.
- com.demo.logging-http: Permite trackear automagicamente el correlationId, todos los requests y responses. Aclaracion: Tiene un fuerte acomplamiento a Spring y el modulo contiene su propio README con la configuracion necesaria para que funcione automagicamente.

Dependencias:
```xml
<dependency>
    <groupId>com.demo</groupId>
    <artifactId>logging</artifactId>
    <version>0.0.1</version>
</dependency>
```
### Datos Generales:

Siguiendo con la "linea" de Logback/Log4j2 se mantiene la interfaz e implementacion a traves de un factory:

```
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
```

Estos son los imports
```java
    import com.demo.logging.core.Logger;
    import com.demo.logging.core.LoggerFactory;
```

Para la construir un mensaje podemos ir componiendo nuestro objeto con lo que necesitemos:

```
    val message = Message("Info Message!") //kotlin
    Message message = new Message("Some message"); //java
```

```kotlin
    message.withEvent(action = "info action!", categories = listOf("CATEGORY"), module="logging")
    message.withTracing("your correlation id")
    message.withHttpRequest("GET", "request body")
    message.withHttpResponse(200, "response body")
    logger.info(message)
```

En caso de un error:

```kotlin
    val error = Message("Error Message!")
    error.withEvent(action = "error action!", categories = listOf("CATEGORY"), module="logging")
    error.withError(Exception("Error!"))

    logger.error(message = error)
```

#### Retrocompatibilidad (solo sintaxis) con Logback (Slf4j)

**Temporalmente** agregamos la retrocompatibilidad con la sintaxis de logback donde solo ingresamos un "string".

Por ej:

```kotlin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExampleSlf4j {
    
    private val logger : Logger = LoggerFactory.getLogger(this::class.java)
    
    fun logs() {
        logger.info("Retro Info Message!")
        logger.error("Error Message", Exception("Error!"))
        logger.warn("Warn message!")
    }

}
```
* Con solo cambiar los imports a la lib de logging, dichos métodos van quedar **deprecados** pero van a seguir logueando de igual manera. 

```kotlin
import com.demo.logging.*
```

*Les recomendamos que se migren al método correcto, construyendo el mensaje como se mencionó anteriormente.*

#### Variables de Entorno: Hay que completarlo con el valor de nuestras apis (algunos ya estan cargadas de por si en el proyecto)

- APPLICATION_NAME=java-app
- LOGGING_SERVICE_NAME=CMDB
- LOGGING_AUTHENTICATION_USER=elastic
- LOGGING_AUTHENTICATION_PASSWORD=algo1234
- LOGGING_HOST=http://localhost:9600
- SERVER_LOGGING_ENABLED=false
- ENVIRONMENT=local  **valores posibles: local-dev-stg-prod**

En ésta versión no vamos a probar el logging a elk, sino de manera "clasica" por stdout. Por ende necesitamos que se configuren las variables:
LOGGING_SERVICE_NAME, LOGGING_AUTHENTICATION_USER, LOGGING_AUTHENTICATION_PASSWORD, LOGGING_HOST con cualquier valor (no van a ser validadas).

Las 2 primeras variables son parte de los indices que se van a generar en elastic search. Las mismas tienen que estar en kebah-case para que elastic search pueda crear el indice de forma correcta.

#### Variables de entorno opcionales:

- SERVER_LOGGING_ENABLED (En ésta versión no vamos a probar el logging a elk, su valor por defecto es "false")
- MY_POD_NAME
- MY_NODE_NAME

Estas ultimas dos variables van a ser leídas solo si tienen realizada la configuracion en okd
Mas info: https://kubernetes.io/docs/tasks/inject-data-application/environment-variable-expose-pod-information/

#### Integracion con Spring para obtener logging "automagico" de requests y responses (incluyendo correlationId)

Tenemos que crear 3 beans mas 1 lista de strings. A continuación detallamos que hace cada uno de los actores:

- HttpTracing: Se encarga de obtener el header "X-CorrelationId" y para identificar el request/response. En el caso de que dicho header NO sea agregado en el request, se crea uno.
- HttpRequestLogging: Se encarga de logguear los requests que llegan al microservicio (*)
- httpResponseLogging: Se encarga de logguear los responses que llegan al microservicio (*)

(*) Aca entra en juego la lista de exclusion, son aquellos endpoints que no queremos que se loggeen automagicamente.

```kotlin 
@Configuration
class LogConfig {
    val exclusionUriList = listOf("/api/health")
    @Bean
    fun httpTracing(): HttpTracing {
        return HttpTracing(exclusionUriList)
    }
    @Bean
    fun httpRequestLogging(): HttpRequestLogging {
        return HttpRequestLogging(exclusionUriList = exclusionUriList)
    }
    
    @Bean
    fun httpResponseLogging(): HttpResponseLogging {
        return HttpResponseLogging(DefaultHttpResponseLogger(exclusionUriList))
    }
}
```