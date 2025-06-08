package qa.frame.config;

import org.springframework.boot.test.context.SpringBootTest;
import io.cucumber.spring.CucumberContextConfiguration;


@SpringBootTest // поднимает контекст приложения
@CucumberContextConfiguration
public class CucumberTestContext {
}
