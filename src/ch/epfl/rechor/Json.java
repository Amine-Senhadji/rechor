package ch.epfl.rechor;

import java.util.List;
import java.util.Map;

/**
 * Represents a JSON value in the ReCHor application.
 * Implemented by records for arrays, objects, strings, and numbers.
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 * @version 1.0
 * @since 1.0
 */
public sealed interface Json {
    /**
     * JSON array value.
     *
     * @param elements the JSON elements in the array
     */
    record JArray(List<Json> elements) implements Json {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (elements.isEmpty()) {
                return sb.append("]").toString();
            }
            boolean first = true;
            for (Json e : elements) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(e.toString());
                first = false;
            }
            return sb.append("]").toString();
        }
    }
    /**
     * JSON object value.
     *
     * @param properties mapping of property names to JSON values
     */
    record JObject(Map<String, Json> properties) implements Json {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            if (properties.isEmpty()) {
                return sb.append("}").toString();
            }
            boolean first = true;
            for (Map.Entry<String, Json> e : properties.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append("\"").append(e.getKey()).append("\"");
                sb.append(":");
                sb.append(e.getValue().toString());
                first = false;
            }
            return sb.append("}").toString();
        }
    }
    /**
     * JSON string value.
     *
     * @param string the string content
     */
    record JString(String string) implements Json {
        @Override
        public String toString() {
            return "\"" + string + "\"";
        }
    }
    /**
     * JSON numeric value.
     *
     * @param value the numeric content
     */
    record JNumber(double value) implements Json {
        @Override
        public String toString() {
            return Double.toString(value);
        }
    }

}
