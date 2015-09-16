package org.jboss.fuse.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.MessageHistory;
import org.apache.camel.NamedNode;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class ScriptUtils {

    private static final SimpleDateFormat format;
    private static final ObjectMapper mapper;

    static {
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        mapper = new ObjectMapper();
        mapper.getSerializationConfig().with(format);
        mapper.addMixInAnnotations(MessageHistory.class, DefaultMessageHistoryMixin.class);
        mapper.addMixInAnnotations(NamedNode.class, NamedNodeMixin.class);
    }

    public static String toIso(Date d) {
        return format.format(d);
    }

    public static String toJson(Object o) {
        try {
            if (o instanceof Collection) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (Object c : (Collection) o) {
                    if (sb.length() > 1) {
                        sb.append(",");
                    }
                    sb.append(toJson(c));
                }
                sb.append("]");
                return sb.toString();
            } else if (o instanceof Map) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                for (Map.Entry<Object, Object> e : ((Map<Object, Object>) o).entrySet()) {
                    if (sb.length() > 1) {
                        sb.append(",");
                    }
                    sb.append(toJson(e.getKey().toString()));
                    sb.append(":");
                    sb.append(toJson(e.getValue()));
                }
                sb.append("}");
                return sb.toString();
            } else if (o == null) {
                return "null";
            } else if (o instanceof Date) {
                return "\"" + toIso((Date) o) + "\"";
            } else if (o instanceof MessageHistory) {
                return mapper.writeValueAsString(o);
            } else {
                return mapper.writeValueAsString(o.toString());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize " + o, e);
        }
    }

    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static interface DefaultMessageHistoryMixin {

        @JsonProperty
        String getRouteId();

        @JsonProperty
        long getElapsed();

        @JsonProperty
        NamedNode getNode();
    }

    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static interface NamedNodeMixin {

        @JsonProperty
        String getId();

        @JsonProperty
        String getShortName();

        @JsonProperty
        String getLabel();
    }
}
