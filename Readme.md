# Demo payOS Spring-boot

## Usage

### Step 1: Download and install Java 17 and make sure you are running this project in Java 17 version

Link: [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

### Step 2: Add payment gateway

- Create `application.properties` base on [`application.properties.example`](./src/main/resources/application.properties.example).

```bash
cp ./src/main/resources/application.properties.example ./src/main/resources/application.properties
```

- Add values for payOS client, all credentials can be get from [my.payos.vn](https://my.payos.vn).

### Step 3: Run the project

- Install dependencies using the command `mvn clean install`
- Run the project using the command `mvn spring-boot:run`
- The project will run on port 8080

> [!NOTE]
> **Webhook Setup (Optional)**: For testing webhook functionality in development, you need to expose your local server using a tool like [ngrok](https://ngrok.com/). The webhook endpoint is `/payment/payos_transfer_handler` (handled by `PaymentController.java`). After exposing your server with ngrok, register the full webhook URL (e.g., `https://abc123.ngrok.io/payment/payos_transfer_handler`) in your payOS dashboard at [my.payos.vn](https://my.payos.vn) or using `/order/confirm-webhook` with your webhook URL.

## Structure

- [`CheckoutController.java`](./src/main/java/com/springboot/app/controller/CheckoutController.java): simple create payment link with base information.
- [`OrderController.java`](./src/main/java/com/springboot/app/controller/OrderController.java): include methods related to payment-request.
- [`PaymentController.java`](./src/main/java/com/springboot/app/controller/PaymentController.java): webhook handler for payment-request.
- [`PayoutsController.java`](./src/main/java/com/springboot/app/controller/PayoutsController.java): include methods related to payouts.
