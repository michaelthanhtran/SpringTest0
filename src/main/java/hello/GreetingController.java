package hello;


import datadog.trace.api.DDTags;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@RestController
public class GreetingController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);
    private final Tracer tracer = GlobalTracer.get();

    @Value("#{environment['sleeptime'] ?: '2000'}")
    private long sleepTime;

    @RequestMapping("/ServiceC")
    public String serviceC() throws InterruptedException {
        Map<String, String> map = new HashMap<>();

        try (Scope scope = tracer.buildSpan("ServiceC").startActive(true)) {
            scope.span().setTag(DDTags.SERVICE_NAME, "springtest0");
            doSomeStuff(scope, "Hello");

            tracer.inject(scope.span().context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
            HttpHeaders header = new HttpHeaders();
            header.setAll(map);

            Thread.sleep(200L);
            doSomeOtherStuff(scope, "World!");
            logger.info("In Service C ***************");
            String rs = restTemplate.postForEntity("http://localhost:9393/ServiceD", new HttpEntity(header), String.class).getBody();
            return rs;

        }
    }

    private String doSomeStuff(Scope scope, String somestring) throws InterruptedException {
        try (Scope scope1 = tracer.buildSpan("doSomeStuff").asChildOf(scope.span()).startActive(true)) {
            scope1.span().setTag(DDTags.SERVICE_NAME, "springtest0");
            System.out.println(somestring);
            Thread.sleep(sleepTime);
        }
        return somestring;
    }


    private void doSomeOtherStuff(Scope scope, String somestring) throws InterruptedException {
        try (Scope scope1 = tracer.buildSpan("doSomeOtherStuff").asChildOf(scope.span()).startActive(true)) {
            scope1.span().setTag(DDTags.SERVICE_NAME, "springtest0");
            System.out.println(somestring);
            Thread.sleep(sleepTime);
        }
    }
}
