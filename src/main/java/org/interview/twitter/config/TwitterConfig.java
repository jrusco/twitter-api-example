package org.interview.twitter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.PrintStream;

@Slf4j
@Configuration
public class TwitterConfig {

    @Bean
    public PrintStream resultStream(@Value("${OUTPUT_FILE:src/main/resources/logs/output.log}") String outputFilePath) throws FileNotFoundException {
        if (StringUtils.isNotEmpty(outputFilePath)){
            return new PrintStream(outputFilePath);
        } else {
            log.debug("msg=[OUTPUT_FILE value not found, defaulting to System.out]");
            return System.out;
        }
    }

    @Bean
    public String bearerToken(@Value("${TWITTER_BEARER_TOKEN}") String bearerToken){
        if (StringUtils.isEmpty(bearerToken)){
            log.error("msg=[TWITTER_BEARER_TOKEN not found: unable to initialize Twitter Client]");
            throw new IllegalArgumentException("TWITTER_BEARER_TOKEN env value is missing");
        }
        return bearerToken;
    }
}
