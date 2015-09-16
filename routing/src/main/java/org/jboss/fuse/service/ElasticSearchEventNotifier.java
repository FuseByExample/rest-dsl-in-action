package org.jboss.fuse.service;

import org.apache.camel.Exchange;
import org.apache.camel.management.event.AbstractExchangeEvent;
import org.apache.camel.management.event.ExchangeSendingEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jboss.fuse.service.ScriptUtils.toJson;

public class ElasticSearchEventNotifier extends EventNotifierSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchEventNotifier.class);

    private Client client;
    private CompiledTemplate compiledTemplate;
    private URL defaultTemplateUrl = getClass().getResource("default.mvel");
    private Map<URL, String> sources = new ConcurrentHashMap<URL, String>();
    private ParserContext context;
    private Map<String, CompiledTemplate> templates = new ConcurrentHashMap<String, CompiledTemplate>();
    private final Map<String, Boolean> perContext = new ConcurrentHashMap<String, Boolean>();
    private final Map<String, Boolean> perRoute = new ConcurrentHashMap<String, Boolean>();
    private AtomicBoolean enabled;
    private final boolean defaultEnable = true;

    private static final String INDEX = "camel";
    private static final String TYPE = "exchange";
    private static int counter = 0;

    @Override
    public void notify(EventObject eventObject) throws Exception {
        if (eventObject instanceof AbstractExchangeEvent) {
            AbstractExchangeEvent aee = (AbstractExchangeEvent) eventObject;
            if (isEnabled(aee.getExchange())) {
                if (aee instanceof ExchangeSendingEvent) {
                    aee.getExchange().getIn().setHeader("AuditCallId", aee.getExchange().getContext().getUuidGenerator().generateUuid());
                }
                String json = toJson(aee);
                IndexResponse response = client.prepareIndex(INDEX, TYPE, String.valueOf(counter++)).setSource(json).execute().get();
                if (response != null) {
                    LOGGER.debug(">> Index JSON document inserted");
                }
            }
        }
    }

    @Override
    public boolean isEnabled(EventObject event) {
        return true;
    }

    @Override
    protected void doStart() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .classLoader(Settings.class.getClassLoader())
                .put("cluster.name", "insight")
                .put("client.transport.sniff", false)
                .build();
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress("fusehost", 9300));

        // Create Mvel ParserContext
        context = new ParserContext();
        try {
            context.addImport("toJson", ScriptUtils.class.getMethod("toJson", Object.class));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unable to find method toJson", e);
        }

        // Load Template
        compiledTemplate = getTemplate();

        enabled = new AtomicBoolean(defaultEnable);
    }

    @Override
    protected void doStop() {
        client.close();
    }

    protected String toJson(AbstractExchangeEvent event) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(EventNotifierSupport.class.getClassLoader());
            String eventType = event.getClass().getSimpleName();
            eventType = eventType.substring("Exchange".length());
            eventType = eventType.substring(0, eventType.length() - "Event".length());

            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("event", eventType);
            vars.put("host", System.getProperty("runtime.id"));
            vars.put("timestamp", new Date());
            vars.put("exchange", event.getExchange());

            return TemplateRuntime.execute(compiledTemplate, context, vars).toString();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private boolean isEnabled(Exchange exchange) {
        Boolean b = isRouteEnabled(exchange);
        if (b == null) {
            b = isContextEnabled(exchange);
        }
        return (b == null) ? enabled.get() : b;
    }

    private Boolean isRouteEnabled(Exchange exchange) {
        if (exchange.getFromRouteId() != null) {
            return perRoute.get(exchange.getFromRouteId());
        } else {
            return true;
        }
    }

    public Boolean isContextEnabled(Exchange exchange) {
        return perContext.get(exchange.getContext().getName());
    }

    private CompiledTemplate getTemplate() {
        String source = getTemplateSource();
        CompiledTemplate template = templates.get(source);
        if (template == null) {
            template = TemplateCompiler.compileTemplate(source, context);
            templates.put(source, template);
        }
        return template;
    }

    private String getTemplateSource() {
        String source = null;
        try {
            source = loadSource(defaultTemplateUrl);
        } catch (IOException e) {
            throw new IllegalStateException("Default template could not be loaded", e);
        }
        return source;
    }

    private String loadSource(URL url) throws IOException {
        String source = sources.get(url);
        if (source == null) {
            source = IOHelpers.loadFully(url);
            sources.put(url, source);
        }
        return source;
    }
}
