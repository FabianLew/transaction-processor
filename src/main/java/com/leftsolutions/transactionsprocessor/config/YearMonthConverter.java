package com.leftsolutions.transactionsprocessor.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
class YearMonthConverter implements Converter<@NotNull String, YearMonth> {

    @Override
    public YearMonth convert(String source) {
        return YearMonth.parse(source);
    }
}
