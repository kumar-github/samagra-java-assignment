# samagra-java-assignment
A simple java assignment for Samagra

## Requirements
* JDK 11+ (currently tested with 11)
* Gradle 6.3+ (currently tested with 6.3)

## How to Build

Please clone this service from below link.

https://github.com/AmphoraInc/CommAPI/tree/C4I2_SP31_V1

Go to ``.../swayam-service/`` and run below commands:

``gradle clean build test -i``

## How to Run

Please check the Swagger documentation of the service for more information.

> The default connection specifications exist in __application.properties__. Modify the content of that file to make
> services use another database.

### Swayam Scraper Service

``Swayam Scraper`` of **swayam-scraper** is ready.

Go to ``.../swayam-service/`` and run below command:

``gradle bootRun``

Once service is up please access below URL:

http://localhost:8001/swagger-ui.html

### To access the H2 in-mem database
http://localhost:8001/h2-console