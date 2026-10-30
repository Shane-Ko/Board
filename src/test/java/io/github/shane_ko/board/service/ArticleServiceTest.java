package io.github.shane_ko.board.service;

import io.github.shane_ko.board.dto.request.ArticleCreateRequest;
import io.github.shane_ko.board.dto.request.ArticleUpdateRequest;
import io.github.shane_ko.board.dto.request.CommentCreateRequest;
import io.github.shane_ko.board.dto.request.MemberCreateRequest;
import io.github.shane_ko.board.entity.Article;
import io.github.shane_ko.board.entity.Member;
import io.github.shane_ko.board.exception.ArticleNotFoundException;
import io.github.shane_ko.board.exception.ForbiddenException;
import io.github.shane_ko.board.repository.ArticleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ArticleServiceTest {

    @Autowired
    ArticleService articleService;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    MemberService memberService;
    @Autowired
    CommentService commentService;
    @Autowired
    EntityManager em;

    Member writer;      // 글 작성자
    Member other;       // 다른 사용자

    @BeforeEach
    void setUp() {
        writer = memberService.create(new MemberCreateRequest("writer1", "작성자", "password123"));
        other = memberService.create(new MemberCreateRequest("other1", "다른사람", "password123"));
    }

    // ===== 작성 / 조회 =====

    @Test
    @DisplayName("게시글 작성 시 로그인한 사용자가 작성자로 연결된다")
    void 게시글_작성_성공() {
        // when
        Article saved = articleService.save(new ArticleCreateRequest("제목", "내용"), writer.getId());

        // then
        Article found = articleService.findOne(saved.getId());
        assertThat(found.getTitle()).isEqualTo("제목");
        assertThat(found.getContent()).isEqualTo("내용");
        assertThat(found.getMember().getId()).isEqualTo(writer.getId());
    }

    @Test
    @DisplayName("없는 id로 조회하면 ArticleNotFoundException")
    void 없는_id로_조회하면_예외() {
        assertThatThrownBy(() -> articleService.findOne(999_999L))
                .isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    @DisplayName("목록은 최신 글(id 내림차순)부터 조회된다")
    void 목록_최신순_조회() {
        // given
        Article first = articleService.save(new ArticleCreateRequest("첫 글", "내용"), writer.getId());
        Article second = articleService.save(new ArticleCreateRequest("두 번째 글", "내용"), other.getId());

        // when
        List<Article> articles = articleService.findAll();

        // then
        assertThat(articles).extracting(Article::getId)
                .containsExactly(second.getId(), first.getId());
    }

    // ===== 수정 (인가) =====

    @Test
    @DisplayName("본인 글은 수정할 수 있다")
    void 본인_글_수정_성공() {
        // given
        Article saved = articleService.save(new ArticleCreateRequest("제목", "내용"), writer.getId());

        // when
        Article updated = articleService.update(saved.getId(),
                new ArticleUpdateRequest("수정된제목", "수정된내용"), writer.getId());

        // then
        assertThat(updated.getTitle()).isEqualTo("수정된제목");
        assertThat(updated.getContent()).isEqualTo("수정된내용");
    }

    @Test
    @DisplayName("다른 사람 글을 수정하면 ForbiddenException")
    void 타인_글_수정시_예외() {
        // given
        Article saved = articleService.save(new ArticleCreateRequest("제목", "내용"), writer.getId());

        // when & then
        assertThatThrownBy(() -> articleService.update(saved.getId(),
                new ArticleUpdateRequest("해킹", "해킹"), other.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    // ===== 삭제 (인가 + FK) =====

    @Test
    @DisplayName("다른 사람 글을 삭제하면 ForbiddenException")
    void 타인_글_삭제시_예외() {
        // given
        Article saved = articleService.save(new ArticleCreateRequest("제목", "내용"), writer.getId());

        // when & then
        assertThatThrownBy(() -> articleService.delete(saved.getId(), other.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("댓글이 달린 게시글도 삭제된다 (FK 제약 위반 재발 방지)")
    void 댓글_있는_글_삭제_성공() {
        // given
        Article saved = articleService.save(new ArticleCreateRequest("제목", "내용"), writer.getId());
        commentService.save(other.getId(), saved.getId(), new CommentCreateRequest("댓글"));

        // when
        articleService.delete(saved.getId(), writer.getId());
        em.flush();     // 테스트는 커밋 없이 롤백되므로, DELETE SQL을 강제로 DB에 보내 FK 제약까지 검사
        em.clear();     // 1차 캐시를 비워 DB에서 다시 조회하도록

        // then
        assertThat(articleRepository.findById(saved.getId())).isEmpty();
    }
}