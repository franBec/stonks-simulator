package dev.pollito.stonks_java.news.application.port.out;

import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.util.List;

public interface NewsClientPortOut {
  List<NewsHeadline> fetchHeadlines();
}
