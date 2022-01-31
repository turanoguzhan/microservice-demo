package com.ouz.microservices.twitter2kafka.service;

import com.ouz.microservices.twitter2kafka.service.config.Twitter2KafkaServiceConfigData;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
/**
 * Scope annotation bean lifecycle'ni belirlemek için kullanılırlar.
 * ( https://www.baeldung.com/spring-bean-scopes ) singleton scope defaulttur.
 * request scope her requestte yeni bean oluşturur. session scope http session boyunca devam eder.
 */
// @Scope(value="request")  // PostConstructor annotation ile birlikte kullanılabilirdi. (1.yol)
public class Twitter2KafkaServiceApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(Twitter2KafkaServiceApplication.class);

    /** constructor injection methodology
     * creates immutable object, it's thread-safe. no need to add annotation (Autowired)
     * no need to reflection so app run faster. get rid of burden of loading app.
     * forces object to be created with injected parameter.
     */
    private final Twitter2KafkaServiceConfigData twitter2KafkaServiceConfigData;

    public Twitter2KafkaServiceApplication(Twitter2KafkaServiceConfigData configData) {
        this.twitter2KafkaServiceConfigData = configData;
    }

    public static void main(String[] args) {
        SpringApplication.run(Twitter2KafkaServiceApplication.class,args);
    }

    /**
     * Uygulama başlangıcı için birden fazla yöntem bulunur.
     */
    /** 1.Yol : PostConstructor ve Request Scope or Session Scope.
    /**
     * PostConstruct method dependency injection tamamlanır tamamlanmaz
     * yani Spring Bean create edilir edilmez yalnızca bir kez çalıştırılır.
     * Spring Bean Singleton olduğu için yalnızca bir kez başlatılır.
     * Bu durumu değiştirmek için class başına Scope belirtilerek
     */
    /*@PostConstruct
    public void init(){

    }*/


    /**
     * 2.yol ise ApplicationEventListener interface'ini implemente ederek, her bir event çağrısında
     * uygulamanın başlangıcını (initilization) sağlamaktır. Burada yine yalnızca bir kez çalışması sağlanmış olur.
     * @param event
     */
    /*@Override
    public void onApplicationEvent(ApplicationEvent event) {

    }*/

    /**
     * 3.yol ise implement CommmandLineRunner implementasyonu.
     * Uygulama başlangıcı için en iyi seçenek budur.
     * ApplicationEventListener implementasyonu ile farkı almış olduğu parametrelerdir.
     */
    @Override
    public void run(String... args) throws Exception {
        //System.out.println("App is starting...");
        // console mesaj log ile yazmak istediğimizde;
        LOG.info("App is starting ....");
        LOG.info(Arrays.toString(twitter2KafkaServiceConfigData.getTwitterKeywords().toArray(new String[]{})));
        LOG.info(twitter2KafkaServiceConfigData.getWelcomeMessage());
    }

}
