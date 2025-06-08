package qa.frame.component;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
@Slf4j
public class TestMemory {
    @Getter
    private Map<String, Object> stash = new HashMap<>();

    public void put(String key, Object value) {
        log.info("[STASH] В память сохранен объект " + value + " по ключу " + key);
        stash.put(key, value);
    }

    public Object get(String key) {
        if (key.contains("{") && key.contains("}")) {
            key = key.replace("{", "").replace("}", "");
        }
        Object value = stash.get(key);
        log.info("[STASH] Из памяти возвращен объект " + value + " по ключу " + key);
        if (value == null)
            log.warn("[STASH] Возвращенно NULL значение по ключу {}", key);
        return value;
    }

    public void remove(String key) {
        stash.remove(key);
    }

    public void clear() {
        stash.clear();
    }


    public String show() {
        log.info("[STASH] Objects:");
        stash.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).forEach(log::info);
        return "#### --> END OF STASH <-- ####";
    }
}
