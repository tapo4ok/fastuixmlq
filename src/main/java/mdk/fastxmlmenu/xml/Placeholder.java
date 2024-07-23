package mdk.fastxmlmenu.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholder {
    public static final Pattern pattern = Pattern.compile("\\$\\{(\\w+)}");
    public Map<String, String> map;
    private static final Placeholder p = new Placeholder();
    public static Placeholder getInstance() {
        return p;
    }
    private Placeholder() {
        this.map = new HashMap<>();
    }
    public String replacePlaceholders(String text) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String value = map.get(matcher.group(1));
            matcher.appendReplacement(sb, value != null ? value : matcher.group(0));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String replacePlaceholders(String text, Map<String, String> maps) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value;
            if (maps.containsKey(key)) {
                value = maps.get(key);
            }
            else {
                value = map.get(key);
            }
            matcher.appendReplacement(sb, value != null ? value : matcher.group(0));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
