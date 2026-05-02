package dev.pollito.stonks_java.config.log;

import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.jspecify.annotations.NonNull;

public class MaskingPatternLayout extends PatternLayout {
  private Pattern multilinePattern;
  private final List<String> maskPatterns = new ArrayList<>();

  public void addMaskPattern(String maskPattern) {
    maskPatterns.add(maskPattern);
    multilinePattern =
        Pattern.compile(String.join("|", maskPatterns), MULTILINE | CASE_INSENSITIVE);
  }

  @Override
  public String doLayout(ILoggingEvent event) {
    return maskMessage(super.doLayout(event));
  }

  private String maskMessage(String message) {
    if (multilinePattern == null) {
      return message;
    }
    return multilinePattern.matcher(message).replaceAll(this::computeReplacement);
  }

  private String computeReplacement(@NonNull MatchResult matchResult) {
    List<String> nonNullGroups =
        IntStream.rangeClosed(1, matchResult.groupCount())
            .mapToObj(matchResult::group)
            .filter(Objects::nonNull)
            .limit(2)
            .toList();

    String replacement =
        switch (nonNullGroups.size()) {
          case 0 -> matchResult.group(0);
          case 1 -> nonNullGroups.getFirst();
          default -> nonNullGroups.getFirst() + "****";
        };

    return quoteReplacement(replacement);
  }
}
