package com.guild.api.demo.user.pact;


import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.guild.api.demo.dao.UserDao;
import com.guild.api.demo.dao.exception.DaoException;
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

public class UserProviderGetUserPactTest {
    @InjectMocks
    private UserDao userDao;

    @Spy
    private RestTemplateExecutor restTemplateExecutor = new RestTemplateExecutor(new RestTemplate(),
            new HystrixExecutor("user_service"),
            new RestEndpointProperties());

    @Mock
    private RestEndpointProperties restEndpointProperties;

    private PactDslJsonBody expectedResult;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }
    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("user_provider", "localhost", 8081, this);


    @Pact(provider="user_provider", consumer="order_service")
    public PactFragment createPact(PactDslWithProvider builder) {
        expectedResult = new PactDslJsonBody()
                .stringType("id")
                .stringType("name")
                .asBody();

        return builder
                .uponReceiving("Get user info response")
                .path("/user-service/users/12345")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(expectedResult)
                .toFragment();
    }

    @Test
    @PactVerification("user_provider")
    public void runTest() throws DaoException {
        given(restTemplateExecutor.getEndpointProperties()).willReturn(restEndpointProperties);
        given(restEndpointProperties.getBaseUrl()).willReturn("http://localhost:8081/user-service");
        UserModel user = userDao.getUser("12345");
        assertEquals(user.getDescription(), expectedResult.toString());
    }
}