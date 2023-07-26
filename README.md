# Fraudulent Transaction Detection Engine

The Fraudulent Transaction Detection Engine is a Spring Boot application that uses Kafka Streams to detect "fraudulent" 
transactions based some simple defined rules. The application processes transaction data in real-time and exposes a HTTP 
endpoint for querying how many fraudulent transactions a given user has posted.

## Features

- Built on Kafka Streams, for processing transaction data in realtime
- Simple rules for detecting three types of suspected fraudulent transactions:
    - Transaction volume > threshold in a period of time
    - Transaction amount > per-user defined limits
    - Transaction timestamp outside a defined grace period
- HTTP endpoint to query for the results
- Built using Spring Boot/Gradle/Lombok

## Requirements

- Built on JDK 17
- Apache Kafka and Zookeeper running locally (this uses the default ports)

## Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/marshallb93/FraudDetectionEngine.git
   ```

2. Configure Kafka:

   Ensure that Zookeeper/Kafka is running locally. This application is currently configured to use the default ports
   though this can be tweaked in `application.yml`

3. Build and Run the Application:

   ```bash
   ./gradlew build
   ./gradlew run
   ```

   This will start the application for detecting fraudulent transactions. N.B. that the application to generate a stream
   of randomly generated transactions (`TransactionGenerationApplication`) must be started **separately**.

## HTTP Endpoints

### Get fraudulent transaction count by user

Returns a list of users who have been involved in fraudulent transactions.

- **URL:** `/fraudulent-transactions/{user-id}`
- **Method:** `GET`
- **Response:** List of users with fraudulent transactions in JSON format

## Customization

You can customize the fraud detection rules and window size in the `application.yml` configuration file to fit your 
specific requirements. These properties are all under the `spring.application.parameters` configuration key.

Similarly the users of the system can be configured in `users.yml`. This allows one to configure a number of different 
_transactors_, which generate random transactions with different behaviour patterns.

## Future Improvements

This is by no means a production-ready application. It is rather a "depth-first" approach (or PoC) that very loosely 
covers the bases of an end-to-end system. Future improvements could include:

- Docker-compose to orchestrate all the necessary containers to bring the system up
- Using an OpenAPI spec to define the HTTP endpoint(s)
- Using a proper schema registry and message format (e.g. Avro or Protobuf) as opposed to manually serializing 
  everything into JSON
- Adding a more robust system for defining rules to flag a transaction as fraudulent
- Fixing known limitations with the rule detecting incorrect timestamps (currently this uses the time that the streams 
  processor reads the message, not when the message was posted)

## Contributors

- [Marshall Bradley](https://github.com/marshallb93)


