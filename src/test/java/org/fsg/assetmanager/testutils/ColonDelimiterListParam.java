package org.fsg.assetmanager.testutils;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.util.Arrays;
import java.util.Collections;

public class ColonDelimiterListParam implements ArgumentConverter {

    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if (source == null) {
            return Collections.emptyList();
        }

        if (!(source instanceof String input)) {
            throw new ArgumentConversionException(
                    String.format("Cannot convert %s to List<String>", source.getClass()));
        }

        if (input.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.asList(input.split(":"));
    }
}
