package io.github.shane_ko.board.repository;

import io.github.shane_ko.board.entity.Comment;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 목록 조회 - 최신순 조회
    @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId ORDER BY c.createdAt DESC")
    List<Comment> findByArticleId(@Param("articleId") Long articleId);


    // 게시글에 달린 댓글 일괄 삭제
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.article.id = :articleId")
    void deleteAllByArticleId(@Param("articleId") Long articleId);
}
