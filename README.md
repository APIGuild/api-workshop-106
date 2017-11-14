# API Capability Building Workshop - Contract Testing

## Setup

#### How to run the services

Install plugin in IntelliJ IDEA: `lombok`

```
./gradlew :user-service:bootRun
./gradlew :logistics-service:bootRun
./gradlew :product-service:bootRun
./gradlew :order-service:bootRun
```

Debug mode: --debug-jvm

#### How to start continuous build
```
./gradlew build --continuous
```

#### Project structure and flow

```
├── README.md
├── order-service
├── user-service
├── product-service
└── logistics-service


http://localhost:8080/order-service/orders/1234567890
-> orderId -> Order User -> userId      -> User Service     
                         -> productId   -> Product Service
                         -> logisticsId -> Logistics Service

```

## Consumer

You can access the two endpoints once you start the services:
- [http://localhost:8080/order-service/orders/1234567890](http://localhost:8080/order-service/orders/1234567890)
- [http://localhost:8080/order-service/orders/1234567890/sync](http://localhost:8080/order-service/orders/1234567890/sync)

### steps
 1. add dependency for pact & configure the output directory of pact file
    ```
    ext {
        PACT_DIR = "../pacts"
    }

    testCompile('au.com.dius:pact-jvm-consumer-junit_2.11:3.5.8')

    ```
    
 2. Add the Pact Rule to your test class to represent your provider.
    ```
    @Rule
        public PactProviderRule mockUserService = new PactProviderRule("user_service", "localhost", 8081, this);

    ```
 3. Annotate a method with Pact that returns a pact fragment for the provider and consumer
    ```
    @Pact(provider="user_service", consumer="order_service")
        public PactFragment createUserPact(PactDslWithProvider builder) {
            expectedUserResponse = new PactDslJsonBody()
                    .stringType("id")
                    .stringType("name")
                    .asBody();
    
            return builder
                    .uponReceiving("Get user info response")
                    .path("/user-service/users/12345")
                    .method("GET")
                    .willRespondWith()
                    .status(200)
                    .body(expectedUserResponse)
                    .toFragment();
        }
    ```
 4. Annotate your test method with PactVerification and write your test inside
    ```
     @Test
     @PactVerification("user_service")
     public void runTest() throws DaoException {
         given(restTemplateExecutor.getEndpointProperties()).willReturn(restEndpointProperties);
         given(restEndpointProperties.getBaseUrl()).willReturn("http://localhost:8081/user-service");
         UserModel user = userDao.getUser("12345");
         assertEquals(user.getDescription(), expectedResult.toString());
     }
    ```

 5. Run your test, it will generate a pact file under '/target/pacts' directory

 ## Provider - User service
 1. Add pact gradle plugin
    ```
    plugins {
        id "au.com.dius.pact" version "3.5.8"
    }
    ```
 2. Define the pacts between your consumers and providers
    ```
    pact {
    	serviceProviders {
    		userService {
    			protocol = 'http'
    			host = 'localhost'
    			port = 8081
    			hasPactWith('orderService') {
    				pactFile = file('../order-service/target/pacts/order_service-user_service.json')
    			}
    		}
    	}
    }
    ```
 3. Execute `./gradlew :user-service:pactVerify`

## Pact Broker
 ### Set up pact broker
 You can use the [Pact Broker Docker container](https://hub.docker.com/r/dius/pact-broker/) or [Terraform on AWS](https://github.com/nadnerb/terraform-pact-broker) or to roll your own...
 
 - Install postgresql. Follow the instruction [here](https://github.com/DiUS/pact_broker-docker/blob/master/POSTGRESQL.md#installation-of-non-docker-postgresql).
 - Create a PostgreSQL (recommended) or MySQL (not recommended, see following note) database.
    ```
    $ psql postgres
    > create database pact_broker;
    > CREATE USER pact_broker WITH PASSWORD 'pact_broker';
    > GRANT ALL PRIVILEGES ON DATABASE pact_broker to pact_broker;

 - Install ruby 2.2.0 or later `brew install ruby` and bundler >= 1.12.0 `gem install bundler`
 - `cd pact-broker`
 - Modify the config.ru and Gemfile as desired (eg. choose database driver gem, set your database credentials. Use the "pg" gem if using Postgres.)
 - Please ensure you use encoding: 'utf8' in your Sequel options to avoid encoding issues.
 - run `bundle`
 - run `bundle exec rackup -p 8088`
 - open [http://localhost:8088](http://localhost:8088)

 ### Publishing pact files to a pact broker

 The pact gradle plugin provides a pactPublish task that can publish all pact files in a directory to a pact broker.

    pact {

        publish {
            pactDirectory = '/pact/dir' // defaults to $buildDir/pacts
            pactBrokerUrl = 'http://localhost:8088'
        }

    }
    
 Execute `./gradlew :order-service:pactPublish` 
 
 ### Verifying pact files from a pact broker
 To set up the validate against the pacts stored in a pact broker, replace the pactLocation in provider with:
    ```
    hasPactsFromPactBroker('http://localhost:8088')
    ```
