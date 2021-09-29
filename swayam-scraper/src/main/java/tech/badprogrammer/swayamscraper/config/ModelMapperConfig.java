package tech.badprogrammer.swayamscraper.config;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * Configuration to yield the {@link ModelMapper} bean with {@link MatchingStrategies.STRICT} matching strategy.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        final ModelMapper result = new ModelMapper();
        result.getConfiguration()
              .setMatchingStrategy(MatchingStrategies.STRICT);
        return result;
    }

    @Bean
    public StringStrippingConverter stringStrippingConverter() {
        final StringStrippingConverter result = new StringStrippingConverter();
        return result;
    }

    @Bean
    public DateToIsoConverter dateToIso() {
        final DateToIsoConverter result = new DateToIsoConverter();
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    public static class StringStrippingConverter extends AbstractConverter<String, String> {

        @Override
        protected String convert(String source) {
            return source != null ? source.strip() : null;
        }
    }

    public static class DateToIsoConverter extends AbstractConverter<Date, String> {
        @Override
        public String convert(Date source) {
            return source != null
                    ? DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(source)
                    : null;
        }
    }
}
