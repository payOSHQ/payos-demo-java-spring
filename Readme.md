#### Step 1: Download and install Java 17 and make sure you are running this project in Java 17 version

Link: [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

#### Step 2: Add payment gateway

- Go to file src/resources/application.properties
- Add values to PayOS payment gateway variables
  - PAYOS_CLIENT_ID={Your Client Id}
  - PAYOS_API_KEY={Your Api Key}
  - PAYOS_CHECKSUM_KEY={Your Checksum Key}

#### Step 3 Run the project

- Install dependencies using the command `mvn clean install`
- Run the project using the command `mvn spring-boot:run`
- The project will run on port 8080
