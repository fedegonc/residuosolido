package com.residuosolido.app.config;

import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Map;
import java.util.Set;

public class I18nDialect extends AbstractProcessorDialect {

    private final JsonMessageSource messageSource;

    public I18nDialect(JsonMessageSource messageSource) {
        super("I18n", "i18n", 1000);
        this.messageSource = messageSource;
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        return Set.of(new TranslationProcessor(messageSource));
    }

    private static class TranslationProcessor extends AbstractElementTagProcessor {

        private final JsonMessageSource messageSource;

        TranslationProcessor(JsonMessageSource messageSource) {
            super(TemplateMode.HTML, null, null, false, null, false, 1500);
            this.messageSource = messageSource;
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                                 IElementTagStructureHandler structureHandler) {
            String key = tag.getAttributeValue("data-i18n");
            String attributes = tag.getAttributeValue("data-i18n-attr");
            if (key == null && attributes == null) return;
            Map<String, String> copies = messageSource.catalogFor(context.getLocale());
            String text = key != null ? copies.get(key) : null;
            if (text != null) structureHandler.setBody(HtmlUtils.htmlEscape(text, "UTF-8"), false);
            if (attributes == null) return;
            for (String pair : attributes.split(",")) {
                String[] parts = pair.trim().split(":", 2);
                if (parts.length != 2 || parts[0].isBlank()) continue;
                String value = copies.get(parts[1].trim());
                if (value != null) {
                    structureHandler.setAttribute(parts[0].trim(), HtmlUtils.htmlEscape(value, "UTF-8"));
                }
            }
        }
    }
}
