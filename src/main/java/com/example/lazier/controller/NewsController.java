package com.example.lazier.controller;

import com.example.lazier.dto.module.NewsDto;
import com.example.lazier.service.Impl.NewsService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

  private final NewsService newsService;
//    private final CacheManager redisCacheManager;

  /**
   * 동작 확인용 // 미사용 컨트롤러,
   * TODO 배포시 주석처리 필요
   */
  @GetMapping("/init")
  public ResponseEntity<?> init() {
    this.newsService.dbInit();
    return ResponseEntity.ok("news DB가 크롤링되었습니다.");
  }

  @GetMapping
  public ResponseEntity<?> showNews(HttpServletRequest request) {
    List<NewsDto> newsDtoList = this.newsService.showNewsByUser(request);
    return ResponseEntity.ok(newsDtoList);
  }

}


