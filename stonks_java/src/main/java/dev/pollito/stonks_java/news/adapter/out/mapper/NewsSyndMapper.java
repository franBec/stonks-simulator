package dev.pollito.stonks_java.news.adapter.out.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = IGNORE)
public interface NewsSyndMapper {

  @Mapping(target = "source", source = "source", qualifiedByName = "toSourceName")
  @Mapping(target = "category", source = "categories", qualifiedByName = "toCategoryName")
  @Mapping(target = "url", source = "link")
  @Mapping(target = "publishedAt", source = "publishedDate", qualifiedByName = "toOffsetDateTime")
  NewsHeadline toHeadline(SyndEntry entry);

  @Named("toSourceName")
  default String toSourceName(SyndFeed source) {
    return source != null && source.getTitle() != null ? source.getTitle() : "Unknown";
  }

  @Named("toCategoryName")
  default String toCategoryName(List<SyndCategory> categories) {
    return categories != null && !categories.isEmpty() ? categories.getFirst().getName() : null;
  }

  @Named("toOffsetDateTime")
  default OffsetDateTime toOffsetDateTime(Date publishedDate) {
    return publishedDate != null ? publishedDate.toInstant().atOffset(UTC) : now();
  }
}
