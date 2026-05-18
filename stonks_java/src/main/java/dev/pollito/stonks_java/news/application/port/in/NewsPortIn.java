package dev.pollito.stonks_java.news.application.port.in;

import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.util.List;

public interface NewsPortIn {
  List<NewsHeadline> getHeadlines();
}
