package qa.frame.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import io.cucumber.datatable.DataTable;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

@Component
@Slf4j
public class Utils {
    private static final String TEST_RESOURCE = System.getProperty("user.dir") + "/src/test/resources/";

    @Autowired
    private DatabaseConnection connection;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestMemory memory;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private VariableManager variableManager;

    /**
     * Загружает JSON из файла и возвращает объект (Map или DTO).
     */
    public <T> T readJsonFromFile(String filePath, Class<T> valueType) throws IOException {
        byte[] jsonData = Files.readAllBytes(Paths.get(filePath));
        return mapper.readValue(jsonData, valueType);
    }


    public String prepareJson(DataTable data) throws JsonProcessingException {
        // Остановим если указано больше 1 файла для инициализации
        assert data.cells().stream()
                .filter(c -> c.get(0)
                        .equalsIgnoreCase("json")
                )
                .count() == 1;

        var cells = data.cells()
                .stream()
                .map(
                        c -> new JsonCell(
                                c.get(0),
                                c.get(1))
                )
                .collect(Collectors.toList()
                );
        AtomicReference<JsonNode> finalJson = new AtomicReference<>();

        Optional<JsonCell> pathCell = cells.stream()
                .filter(cell -> cell.label.equalsIgnoreCase("json"))
                .findFirst();

        if (pathCell.isPresent()) {
            var path = pathCell.get().getValue();
            if(path != null && !path.isEmpty()) {
                log.info("PATH: {}", path);
                InputStream istream = null;
                try {
                    istream = new FileInputStream(TEST_RESOURCE + path);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                log.info(TEST_RESOURCE + path);
                try {
                    finalJson.set(mapper.readTree(istream));
                    log.info("Создан Json-объект на основе " + TEST_RESOURCE + path);
                    log.info("###  --> JSON <-- ###");
                    log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalJson.get()));
                    log.info("###  --> END OF JSON <-- ###");
                } catch (IOException e) {
                    log.error("Не найден JSON файл " + TEST_RESOURCE + path);
                    throw new RuntimeException(e);
                } finally {
                    cells.remove(pathCell.get());
                }
            }
        } else {
            // создаем пусто json если не передали
            finalJson.set(mapper.createObjectNode());
            log.info("Создан пустой JSON-объект");
        }

        cells.forEach(cell -> {
            setJsonValue((ObjectNode) finalJson.get(), cell);
        });

        String result = mapper.writeValueAsString(finalJson.get());
        return result;
    }

    /**
     *
     * @param root ~(ObjectNode) JsonNode
     * @param jc – JsonCell
     */
    private void setJsonValue(ObjectNode root, JsonCell jc) {
        String path = jc.getLabel();
        String value = jc.getValue();
        if (value.contains("{")) {
            var stashKey = value.replace("{", "").replace("}", "");
            value = memory.get(stashKey).toString();
        }

        String[] parts = path.split("\\.");

        ObjectNode currentNode = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];

            if (!currentNode.has(part) || !currentNode.get(part).isObject()) {
                currentNode.set(part, mapper.createObjectNode());
            }
            currentNode = (ObjectNode) currentNode.get(part);
        }

        String lastPart = parts[parts.length - 1];

        // Пытаемся привести к числу, если это возможно
        if (value.matches("-?\\d+")) {
            currentNode.put(lastPart, Integer.parseInt(value));
        } else if (value.matches("-?\\d+\\.\\d+")) {
            currentNode.put(lastPart, Double.parseDouble(value));
        } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            currentNode.put(lastPart, Boolean.parseBoolean(value));
        } else {
            currentNode.put(lastPart, value);
        }
    }

    public static boolean evaluteExpression(
            JsonPath jsonPath,
            String expression
    ) {
        var res =  jsonPath.getBoolean(expression);
        log.info("[JSON] {}", res);
        return res;
    }

    public RequestSpecification buildRequestSpecificationOnTable(DataTable dataTable) {
//        log.info(dataTable.cells().toString());
        List<RequestCell> cells = dataTable.cells()
                .stream()
                .map(
                        c -> new RequestCell(c.get(0), c.get(1), c.get(2))
                )
                .collect(Collectors.toList());

        RequestSpecBuilder specification = new RequestSpecBuilder();
        Map<String, String> params = new HashMap<>();

//      Параметры запроса
        cells.forEach(cell -> {
            switch (cell.getLabel().toUpperCase()) {
                case "HEADER":
                    specification.addHeader(cell.getType(), cell.getValue());
                    log.info("Header: " + cell.getValue());
                    break;
                case "BODY":
                    String body = (String) memory.get(cell.getType());
                    specification.setBody(body);
                    break;
                case "PATH":
//                    TODO fix add to path
//                    log.info("Path: " + cell.getType());
//                    specification.setBasePath(cell.getType());
                    break;
                case "PARAMETER":
                    log.info("Parameter: " + cell.getType() + " " + cell.getValue());
                    params.put(cell.getType(), cell.getValue());
                    break;
            }

            if (!params.isEmpty()) {
                specification.addParams(params);
            }
        });

        return specification.build();
    }

    public Response sendGet(String url, RequestSpecification request) {
        Response response = given()
                .spec(request)
                .when()
                .get(url);

        return response;
    }

    public Response sendPost(String url, RequestSpecification request) {
        log.info("Requset: " + request.toString());

        Response response = given()
                .spec(request)
                .when()
                .post(url);
        return response;
    }

    public Response sendPut(String finalUrl, RequestSpecification request) {
        log.info("Requset: " + finalUrl);

        Response response = given()
                .spec(request)
                .when()
                .put(finalUrl);

        return response;
    }

    public String prepareQuery(String stashKey, DataTable table) {
        List<SqlCell> cells = table.cells().stream()
                .map(
                        c -> new SqlCell(
                                c.get(0),
                                c.get(1)
                        )
                )
                .collect(Collectors.toList());

        StringBuilder query = new StringBuilder("select");
        for (SqlCell cell : cells) {
            String keyword = cell.keyWord.toLowerCase();
            String parsedValue = variableManager.parseV2(cell.value);

            switch (keyword) {
                // Простые добавления без значения
                case "asc":
                case "desc":
                case "is null":
                case "is not null":
                case "case":
                case "else":
                case "end":
                    query.append(" ").append(keyword);
                    break;

                // Операторы, требующие значения
                case "select":
                    query.append(" ").append(parsedValue);
                    break;
                case "from":
                case "where":
                case "and":
                case "or":
                case "group by":
                case "having":
                case "order by":
                case "join":
                case "left join":
                case "right join":
                case "inner join":
                case "on":
                case "union":
                case "select distinct":
                case "limit":
                case "offset":
                case "between":
                case "like":
                case "when":
                case "then":
                    query.append(" ").append(keyword);
                    query.append(" ").append(parsedValue);
                    break;

                // Операторы со скобками
                case "in":
                case "exists":
                    query.append(" ").append(keyword).append(" (").append(parsedValue).append(")");
                    break;

                default:
                    throw new IllegalArgumentException("[SQL] не поддерживается: " + cell.keyWord);
            }
        }

        return query.toString();
    }

    public List<Map<String, Object>> executeQuery(String query) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        log.info("[SQL] Выполняю {}", query);
        try(
                Statement statement = connection.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(query)
        ) {

            ResultSetMetaData meta = rs.getMetaData();
            int columns = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columns; i++) {
                    row.put(
                            meta.getColumnLabel(i),
                            rs.getObject(i)
                    );
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            log.error("[SQL] Ошибка при выоплнения запроса {}", query);
            throw new RuntimeException(e);
        }

        return resultList;
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private class RequestCell {
        private String label;
        private String type;
        private String value;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private class JsonCell {
        private String value;
        private String label;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private class SqlCell {
        private String keyWord;
        private String value;

    }
}
