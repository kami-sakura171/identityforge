package com.identityforge.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public final class CsvParser {

    private static final int MAX_ROWS = 1000;

    private CsvParser() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static CsvParseResult parse(InputStream inputStream, List<String> requiredHeaders) throws IOException {
        List<Map<String, String>> validRows = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(reader)) {

            Map<String, Integer> headerMap = parser.getHeaderMap();
            for (String required : requiredHeaders) {
                if (!headerMap.containsKey(required.toLowerCase())) {
                    throw new IOException("Missing required column: " + required);
                }
            }

            int rowNum = 1;
            for (CSVRecord record : parser) {
                rowNum++;
                if (rowNum - 1 > MAX_ROWS) {
                    errors.add(new RowError(rowNum, "CSV contains more than " + MAX_ROWS + " rows"));
                    break;
                }

                Map<String, String> rowMap = new HashMap<>();
                for (String header : requiredHeaders) {
                    rowMap.put(header, record.get(header));
                }
                validRows.add(rowMap);
            }
        }

        return new CsvParseResult(validRows, errors);
    }

    public record CsvParseResult(List<Map<String, String>> validRows, List<RowError> errors) {}

    public record RowError(int row, String reason) {}
}
