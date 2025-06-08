package qa.frame.steps;


import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import qa.frame.component.DatabaseConnection;
import qa.frame.component.TestMemory;
import qa.frame.component.Utils;
import qa.frame.component.VariableManager;
import qa.frame.config.SpringContext;

import java.math.BigDecimal;
import java.util.*;

import static io.restassured.RestAssured.given;

@Slf4j
public class BasicApiSteps {

    private final  TestMemory memory;
    private final VariableManager varManager;
    private final Utils utils;
    private final DatabaseConnection connection;

    public BasicApiSteps() {
        this.memory = SpringContext.getBean(TestMemory.class);
        this.varManager = SpringContext.getBean(VariableManager.class);
        this.utils = SpringContext.getBean(Utils.class);
        this.connection = SpringContext.getBean(DatabaseConnection.class);
    }

    //    @Step
    @И("создан json-файл с параметрами из таблицы и сохранить по ключу '([^']+)'$")
    public void packJson(String stashKey, DataTable dataTable) throws Exception {
        String json = utils.prepareJson(dataTable);
        memory.put(stashKey, json);
    }

//    @Step
    @И("по ключу '([^']+)' сохраняется значение '([^']+)' с типом '([^']+)'$")
    public void saveVariable(String stashKey, String value, String type) {
        switch (type) {
            case "string":
                memory.put(stashKey, String.valueOf(value));
                break;
            case "number":
                memory.put(stashKey, new BigDecimal(value));
                break;
            case "boolean":
                memory.put(stashKey, Boolean.parseBoolean(value));
                break;
            case "int":
                memory.put(stashKey, Integer.parseInt(value));
                break;
            case "integer":
                memory.put(stashKey, Integer.parseInt(value));
                break;
            default:
                memory.put(stashKey, value);
                break;
        }
    }

//    @Step
    @И("ответ '([^']+)' имеет статус-код '([^']+)'$")
    public void checkStatusCode(String stashKey, int status) throws Exception {
        Response response = ((Optional<Response>) memory.get(stashKey)).get();
        var statusCode = response.getStatusCode();
        Assert.assertEquals(status, statusCode);
    }

//    @Step
    @И("проверить в теле ответа '([^']+)' значение по JsonPath '([^']+)' '([^']+)' '([^']+)'$")
    public void evaluteBoolean(
            String stashKey,
            String condition,
            String equalOrNot,
            String value
    ) throws Exception {
        Response response = ((Optional<Response>) memory.get(stashKey)).get();
        Boolean result = utils.evaluteExpression(
                response.jsonPath(),
                condition
        );
        log.info("[JSON] проверка {} {} {}", condition, equalOrNot, value);
        switch (equalOrNot.toLowerCase()) {
            case "равно":
                Assert.assertEquals(
                        result,
                        Boolean.parseBoolean(value)
                );
                break;
            case "не равно":
                Assert.assertEquals(
                        !result,
                        Boolean.parseBoolean(value)
                );
                break;
            default:
                log.warn("[JSON] используйте операции 'равно' и 'не равно'");
                throw new IllegalArgumentException();
        }
    }


//    @Step
    @И("значение по JsonPath '([^']+)' из тела ответа '([^']+)' сохраняется по ключу '([^']+)' с типом '([^']+)'$")
    public void saveValueFromJsonPathWithType(String path, String responseStashKey, String stashKey, String type )
    {
        Response response = ((Optional<Response>) memory.get(responseStashKey)).get();
        switch (type) {
            case "str":
            case "String":
            case "string":
                var v1 = response.jsonPath().getString(path);
                memory.put(stashKey, String.valueOf(v1));
                break;
            case "Integer":
            case "Int":
            case "int":
            case "number":
                int v2 = response.jsonPath().getInt(path);
                memory.put(stashKey, new BigDecimal(v2));
                break;
            case "bool":
            case "Boolean":
            case "boolean":
                boolean v3 = response.jsonPath().getBoolean(path);
                memory.put(stashKey, v3);
                break;
            default:
                log.info("[JSON] поддерживаются типы int, string и boolean");
                throw new IllegalArgumentException();
        }
    }

    @И("значение '([^']+)' '([^']+)' значению '([^']+)'$")
    public void compareValues(String stashKey1, String formatter, String stashKey2) throws Exception {
        var v1 = memory.get(stashKey1);
        boolean r;
        if (v1 instanceof String) {
            String v2 = (String) memory.get(stashKey2);
            log.info("[STEP] Сравниваю значения {} и {}", v1, v2);
            r = v1.equals(v2);
        } else if (v1 instanceof Integer) {
            int v2 = (int) memory.get(stashKey2);
            log.info("[STEP] Сравниваю значения {} и {}", v1, v2);
            r = v1.equals(v2);
        } else if (v1 instanceof Boolean) {
            boolean v2 = (boolean) memory.get(stashKey2);
            log.info("[STEP] Сравниваю значения {} и {}", v1, v2);
            r = v1.equals(v2);
        } else {
            log.error("[STEP] Ошибка приведения типа переменной из памяти");
            throw new IllegalArgumentException();
        }

        switch (formatter) {
            case "равно":
                Assert.assertTrue(r);
                break;
            case "не равно":
                Assert.assertFalse(r);
                break;
            default:
                log.error("[STEP] Используйте операции 'равно' и 'не равно'");
                throw new IllegalArgumentException();
        }
    }

//    @Step
    @И("^выполнен '([^']+)' запрос к URL '([^']+)' и параметрами из таблицы и сохранен по ключу '([^']+)'$")
    public void prepareRequestSpec(String method, String url, String stashKey) {
        DataTable dataTable = null;
        String finalUrl = url.contains("{") ? varManager.parseV2(url) : url;
        prepareRequestSpec(method, finalUrl, stashKey, dataTable);
    }

//    @Step
    @И("^выполнен '([^']+)' запрос по '([^']+)' и параметрами из таблицы и сохранен по ключу '([^']+)'$")
    public void prepareRequestSpec(String method, String url, String stashKey, DataTable dataTable) {
        String finalUrl = url.contains("{") ? varManager.parseV2(url) : url;

        RequestSpecification request;
        if (dataTable == null)
            request = given();
        else
            request = utils.buildRequestSpecificationOnTable(dataTable);

        log.info("[URL] Final url: {}", finalUrl);



        Optional<Response> response =  Optional.empty();
        switch (method) {
            case "GET":
                response = Optional.of(utils.sendGet(finalUrl, request));
                memory.put(stashKey, response);
                break;
            case "POST":
                response = Optional.of(utils.sendPost(finalUrl, request));
                memory.put(stashKey, response);
                break;
            case "PUT":
                response = Optional.of(utils.sendPut(finalUrl, request));
                memory.put(stashKey, response);
                break;
            default:
                throw new IllegalArgumentException("Метод " + method + " не поддерживается");
        }
        log.info("### --> RESPONSE <-- ###");
        response.ifPresent(r -> log.info(r.asPrettyString()));
        log.info("### --> END OF RESPONSE <-- ###");
    }

//    @Step
    @И("^выполнен SQL запрос '([^']+)' ответ сохранен по ключу '([^']+)' в формате '([^']+)'$")
    public void executeQueryAndSaveResult(String query, String stashKey, String type) {
        String finalQuery = varManager.parseV2(query);
        List<Map<String, Object>> result = utils.executeQuery(finalQuery);
        if (result.size() == 1) {
            Object obj = result.get(0).entrySet()
                    .stream()
                    .findFirst()
                    .get()
                    .getValue();
            switch (type) {
                case "str":
                case "String":
                case "string":
                    memory.put(stashKey, String.valueOf(obj));
                    break;
                case "Integer":
                case "Int":
                case "int":
                case "number":
                    memory.put(stashKey, new BigDecimal(obj.toString()));
                    break;
                case "bool":
                case "Boolean":
                case "boolean":
                    memory.put(stashKey, Boolean.parseBoolean(obj.toString()));
                    break;
                default:
                    log.info("[JSON] поддерживаются типы int, string и boolean");
                    throw new IllegalArgumentException();
            }
        }
    }

//    @Step
    @И("^сохранен SQL запрос с параметрами из табоицы и сохранен по ключу '([^']+)'$")
    public void prepareAndSaveQuery(String stashKey, DataTable table) {
        String query = utils.prepareQuery(stashKey, table);
        log.info("[SQL] Подготовлен запрос {}", query);
        memory.put(stashKey, query.toString());
    }

}
