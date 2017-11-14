package com.guild.api.demo.user.pact;


import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import com.guild.api.demo.dao.UserDao;
import com.guild.api.demo.util.hystrix.HystrixExecutor;
import com.guild.api.demo.util.rest.RestEndpointProperties;
import com.guild.api.demo.util.rest.RestTemplateExecutor;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.web.client.RestTemplate;

import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

public class OrderServicePactTest {
    @InjectMocks
    private UserDao userDao;


    @Spy
    private RestTemplateExecutor restTemplateExecutor = new RestTemplateExecutor(new RestTemplate(),
            new HystrixExecutor("service"),
            new RestEndpointProperties());

    @Mock
    private RestEndpointProperties restEndpointProperties;

    private PactDslJsonBody expectedUserResponse;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        given(restTemplateExecutor.getEndpointProperties()).willReturn(restEndpointProperties);
    }


}