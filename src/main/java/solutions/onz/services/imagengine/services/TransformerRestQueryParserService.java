package solutions.onz.services.imagengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TransformerRestQueryParserService {
    // Allowed operations
    private final String OPERATIONS = "resize|crop|rotate|grayscale|sepia|stitch|sticker|watermark";
    // Pattern for operation
    private final Pattern OPERATION_PATTERN = Pattern.compile(
            "\\b(" + OPERATIONS + ")\\((.*?)\\)"
    );
    // Pattern for parameters
    private final Pattern PARAM_PATTERN = Pattern.compile(
            "(\\w+)=([^,]+)"
    );

    /**
     * Parse the formula string into a map of operations and their parameters
     *
     * @param formula the formula string
     * @return a map of operations and their parameters
     */
    public Mono<Map<String, Map<String, String>>> parseFormula(String formula) {
        return Mono.fromCallable(() -> {
            Map<String, Map<String, String>> result = new HashMap<>();

            Matcher operationMatcher = OPERATION_PATTERN.matcher(formula);
            while (operationMatcher.find()) {
                String operationName = operationMatcher.group(1).toUpperCase();
                String params = operationMatcher.group(2);
                Map<String, String> paramMap = new HashMap<>();

                Matcher paramMatcher = PARAM_PATTERN.matcher(params);
                while (paramMatcher.find()) {
                    String key = paramMatcher.group(1);
                    String value = paramMatcher.group(2);
                    paramMap.put(key, value);
                }

                result.put(operationName, paramMap);
            }


            return result;
        });
    }


}
