package com.residuosolido.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.thymeleaf.exceptions.TemplateEngineException;
import org.thymeleaf.exceptions.TemplateInputException;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class ThymeleafErrorLogger {

    private static final Logger log = LoggerFactory.getLogger(ThymeleafErrorLogger.class);

    @ExceptionHandler({ TemplateInputException.class })
    public ResponseEntity<String> handleTemplateInput(TemplateInputException ex, HttpServletRequest request) {
        logTemplateException("TemplateInputException", ex, request);
        // Optional: render a minimal safe error instead of default error page
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error de plantilla. Consulte los logs con el requestId=" + MDC.get("requestId"));
    }

    @ExceptionHandler({ TemplateEngineException.class })
    public ResponseEntity<String> handleTemplateEngine(TemplateEngineException ex, HttpServletRequest request) {
        logTemplateException("TemplateEngineException", ex, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error al procesar la plantilla. Consulte los logs con el requestId=" + MDC.get("requestId"));
    }

    private void logTemplateException(String type, TemplateEngineException ex, HttpServletRequest request) {
        String requestId = MDC.get("requestId");
        String url = request.getRequestURI();
        String method = request.getMethod();

        String templateName = null;
        Integer line = null;
        Integer col = null;
        if (ex instanceof TemplateInputException tie) {
            templateName = tie.getTemplateName();
            line = tie.getLine();
            col = tie.getCol();
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("type", type);
        details.put("requestId", requestId);
        details.put("method", method);
        details.put("url", url);
        if (templateName != null) details.put("template", templateName);
        if (line != null) details.put("line", line);
        if (col != null) details.put("col", col);
        details.put("message", ex.getMessage());

        log.error("[thymeleaf-error] {}", details, ex);
    }
}
