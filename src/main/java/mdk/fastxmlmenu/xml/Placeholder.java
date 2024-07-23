package mdk.fastxmlmenu.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholder {
    public static final Pattern pattern = Pattern.compile("\\$\\{(\\w+)(?:\\.(toLowerCase|toUpperCase|intern|trim))?}");
    public Map<String, String> map;
    private static final Placeholder p = new Placeholder();

    public static Placeholder getInstance() {
        return p;
    }

    private Placeholder() {
        this.map = new HashMap<>();
    }

    public String replacePlaceholders(String text) {
        return replacePlaceholders(text, null);
    }

    public String replacePlaceholders(String text, Map<String, String> additionalMap) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String action = matcher.group(2);
            String value = null;

            // Проверяем дополнительные карты, если они предоставлены
            if (additionalMap != null && additionalMap.containsKey(key)) {
                value = additionalMap.get(key);
            } else if (map.containsKey(key)) {
                value = map.get(key);
            }

            // Применяем действие, если оно указано
            if (value != null) {
                if ("toLowerCase".equals(action)) {
                    value = value.toLowerCase();
                } else if ("toUpperCase".equals(action)) {
                    value = value.toUpperCase();
                } else if ("intern".equals(action)) {
                    value = value.intern();
                } else if ("trim".equals(action)) {
                    value = value.trim();
                }
            }

            // Если значение найдено, заменяем шаблон на значение, иначе оставляем шаблон без изменений
            matcher.appendReplacement(sb, value != null ? value : matcher.group(0));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
