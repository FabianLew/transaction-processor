package com.leftsolutions.transactionsprocessor.importing.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.YearMonth;

@Mapper(componentModel = "spring")
interface ImportJobMapper {

    @Mapping(target = "month", expression = "java(toYearMonth(document.getYear(), document.getMonth()))")
    ImportJobStatusDto toDto(ImportJobDocument document);

    default YearMonth toYearMonth(int year, int month) {
        return YearMonth.of(year, month);
    }
}
