package io.github.shane_ko.board.repository;

import io.github.shane_ko.board.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 게시글 목록 + 작성자를 한 번에 조회 (N+1 방지)
    @Query("SELECT a FROM Article a JOIN FETCH a.member ORDER BY a.id DESC")
    List<Article> findAllWithMember();
}
