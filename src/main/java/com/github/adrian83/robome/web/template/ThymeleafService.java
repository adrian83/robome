package com.github.adrian83.robome.web.template;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ThymeleafService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThymeleafService.class);
    
    private final TemplateEngine templateEngine;

    @Inject
    public ThymeleafService() {
        this.templateEngine = createTemplateEngine();
        LOGGER.info("ThymeleafService initialized");
    }

    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // Set to true in production
        
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        
        return engine;
    }

    public String processTemplate(String templateName, Map<String, Object> variables) {
        LOGGER.debug("Processing template: {} with variables: {}", templateName, variables);
        
        Context context = new Context();
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        
        // Add common variables available to all templates
        context.setVariable("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        context.setVariable("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        try {
            String result = templateEngine.process(templateName, context);
            LOGGER.debug("Template processed successfully: {}", templateName);
            return result;
        } catch (Exception e) {
            LOGGER.error("Error processing template: {}", templateName, e);
            return createErrorPage(e);
        }
    }

    private String createErrorPage(Exception e) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Template Error</title>
                <style>
                    body { font-family: Arial, sans-serif; padding: 20px; }
                    .error { background: #ffebee; border: 1px solid #f44336; padding: 15px; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="error">
                    <h2>Template Processing Error</h2>
                    <p><strong>Error:</strong> %s</p>
                </div>
            </body>
            </html>
            """.formatted(e.getMessage());
    }
}
