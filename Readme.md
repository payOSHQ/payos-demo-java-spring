#### Step 1: Download and install Java 16 and make sure you are running this project in Java 16 version
Link: [Java 16](https://www.oracle.com/java/technologies/javase/jdk16-archive-downloads.html)

#### Step 2: Add payment gateway
- Go to file src/resources/application.properties
- Add values to PayOS payment gateway variables
   - PAYOS_CLIENT_ID={Your Client Id}
   - PAYOS_API_KEY={Your Api Key}
   - PAYOS_CHECKSUM_KEY={Your Checksum Key}
#### Step 3 Run the project
- **Vs code**:
   - Need to install extensions Extension Pack for Java and Debugger for Java.
   - Points to file src/main/java/com/springboost/app/SpringboostBackendPayosApplication.java
   - F5 or Run -> Start Debugging
- **IntelliJ IDEA**:
   - Make sure IntelliJ has received Java version 16
   - Points to file src/main/java/com/springboost/app/SpringboostBackendPayosApplication.java
   - Run