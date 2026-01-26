package com.leftsolutions.transactionsprocessor.shared;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
class YearMonthConverter implements Converter<String, YearMonth> {

    @Override
    public YearMonth convert(String source) {
        return YearMonth.parse(source);
    }
}
