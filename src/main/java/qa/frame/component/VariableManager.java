package qa.frame.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class VariableManager {
    @Autowired
    private TestMemory memory;
    private ObjectMapper mapper = new ObjectMapper();

    public void setVariable(String key, String value, String type) {
        Object toStore = value;
        switch (type.toLowerCase()) {
            case "int":
                toStore = Integer.parseInt(value);
                break;
            case "str":
            case "string":
                // оставляем строку
                break;
            case "boolean":
            case "bool":
                toStore = Boolean.parseBoolean(value);
        }
        memory.put(key, toStore);
    }

    public String parseV2(String example) {
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(example);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1); // ключ внутри фигурных скобок
            log.info("[STASH] Поиск замены переменной для {}", key);
            String value = (String) memory.get(key);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
