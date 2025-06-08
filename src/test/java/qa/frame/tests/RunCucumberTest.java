package qa.frame.tests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        // останавливает тест с ошибкой, при встрече не раализованного шага
//        strict = false,
        // при false предупреждение будет выдаваться по достижении неразработанного шага
        dryRun = false,
        // отображение логов
        monochrome = true,
        // формирование allure отчета
        plugin = {
                "pretty",
                "summary",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",  // <-- движок аллюра
                "html:target/cucumber-reports/html-report",
                "json:target/cucumber-reports/cucumber.json"
        },
//        plugin = {"pretty"},
        // Обязательные параметры
        // указывает, где лежат файлы .features (файлы с тестовыми сценариями)
        features = {"src/test/resources/features"},
        // указывает, где лежат шаги
        glue = {"qa.frame.steps", "qa.frame.config"},

        tags = "@smoke"
)
public class RunCucumberTest {
}
