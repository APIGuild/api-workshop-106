package com.guild.api.demo.user.pact;


import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.guild.api.demo.dao.LogisticsDao;
import com.guild.api.demo.dao.ProductDao;
import com.guild.api.demo.dao.UserDao;
import com.guild.api.demo.dao.exception.DaoException;
import com.guild.api.demo.model.LogisticsModel;
import com.guild.api.demo.model.ProductModel;
import com.guild.api.demo.model.UserModel;
import com.guild.api.demo.util.hystrix.HystrixExecutor;
import com.guild.api.demo.util.rest.RestEndpointProperties;
import com.guild.api.demo.util.rest.RestTemplateExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

public class OrderServicePactTest {
    @InjectMocks
    private UserDao userDao;

    @InjectMocks
    private ProductDao productDao;

    @InjectMocks
    private LogisticsDao logisticsDao;

    @Spy
    private RestTemplateExecutor restTemplateExecutor = new RestTemplateExecutor(new RestTemplate(),
            new HystrixExecutor("service"),
            new RestEndpointProperties());

    @Mock
    private RestEndpointProperties restEndpointProperties;

    private PactDslJsonBody expectedUserResponse;
    private PactDslJsonBody expectedProductResponse;
    private PactDslJsonBody expectedLogisticsResponse;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        given(restTemplateExecutor.getEndpointProperties()).willReturn(restEndpointProperties);
    }
    @Rule
    public PactProviderRule mockUserService = new PactProviderRule("user_service", "localhost", 8081, this);

    @Rule
    public PactProviderRule mockProductService = new PactProviderRule("product_service", "localhost", 8083, this);

    @Rule
    public PactProviderRule mockLogisticService = new PactProviderRule("logistics_service", "localhost", 8082, this);


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

    @Pact(provider="product_service", consumer="order_service")
    public PactFragment createProductPact(PactDslWithProvider builder) {
        expectedProductResponse = new PactDslJsonBody()
                .stringType("id")
                .stringType("name")
                .asBody();

        return builder
                .uponReceiving("Get product info response")
                .path("/product-service/products/12345")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(expectedProductResponse)
                .toFragment();
    }

    @Pact(provider="logistics_service", consumer="order_service")
    public PactFragment createLogisticPact(PactDslWithProvider builder) {
        expectedLogisticsResponse = new PactDslJsonBody()
                .stringType("id")
                .stringType("logistics")
                .asBody();

        return builder
                .uponReceiving("Get logistic info response")
                .path("/logistics-service/logistics/12345")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(expectedLogisticsResponse)
                .toFragment();
    }

    @Test
    @PactVerification("user_service")
    public void runUserTest() throws DaoException {
        given(restEndpointProperties.getBaseUrl()).willReturn("http://localhost:8081/user-service");
        UserModel user = userDao.getUser("12345");
        assertEquals(user.getDescription(), expectedUserResponse.toString());
    }

    @Test
    @PactVerification("product_service")
    public void runProductTest() throws DaoException {
        given(restEndpointProperties.getBaseUrl()).willReturn("http://localhost:8083/product-service");
        ProductModel product = productDao.getProduct("12345");
        assertEquals(product.getDescription(), expectedProductResponse.toString());
    }

    @Test
    @PactVerification("logistics_service")
    public void runLogisticsTest() throws DaoException {
        given(restEndpointProperties.getBaseUrl()).willReturn("http://localhost:8082/logistics-service");
        LogisticsModel logistics = logisticsDao.getLogistics("12345");
        assertEquals(logistics.getDescription(), expectedLogisticsResponse.toString());
    }
}