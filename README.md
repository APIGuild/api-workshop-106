# API Capability Building Workshop - Contract Test

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

    testCompile('au.com.dius:pact-jvm-consumer-junit_2.11:3.2.11')

    ```
    
 2. Add the Pact Rule to your test class to represent your provider.
    ```
    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("user_service", this);

    ```
 3. Annotate a method with Pact that returns a pact fragment for the provider and consumer
    ```
    @Pact(provider="user_service", consumer="order_service")
        public PactFragment createPact(PactDslWithProvider builder) {
            return builder
                    .given("test state")
                    .uponReceiving("ExampleJavaConsumerPactRuleTest test interaction")
                    .path("/")
                    .method("GET")
                    .willRespondWith()
                    .status(200)
                    .body("{\"responsetest\": true}")
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
        id "au.com.dius.pact" version "3.2.11"
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
 3. Execute `gradle pactVerify`

##Pact Broker

 ###Publishing pact files to a pact broker

    The pact gradle plugin provides a pactPublish task that can publish all pact files in a directory to a pact broker.
    ```
    pact {

        publish {
            pactDirectory = '/pact/dir' // defaults to $buildDir/pacts
            pactBrokerUrl = 'http://pactbroker:1234'
        }

    }
    ```
 ###Verifying pact files from a pact broker
    To set up the validate against the pacts stored in a pact broker, replace the pactLocation in provider with:
    ```
    hasPactsFromPactBroker('http://pact-broker:5000/')
    ```