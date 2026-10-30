package io.github.shane_ko.board.service;

import io.github.shane_ko.board.dto.request.ArticleUpdateRequest;
import io.github.shane_ko.board.entity.Article;
import io.github.shane_ko.board.dto.request.ArticleCreateRequest;
import io.github.shane_ko.board.entity.Comment;
import io.github.shane_ko.board.entity.Member;
import io.github.shane_ko.board.exception.ArticleNotFoundException;
import io.github.shane_ko.board.exception.ForbiddenException;
import io.github.shane_ko.board.exception.MemberNotFoundException;
import io.github.shane_ko.board.repository.ArticleRepository;
import io.github.shane_ko.board.repository.CommentRepository;
import io.github.shane_ko.board.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;


    // Create
    @Transactional
    public Article save(ArticleCreateRequest form, Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
        // 1. dto -> entity 로 변환 (toEntity())
        return articleRepository.save(form.toEntity(member));
    }

    //ReadOne
    public Article findOne(Long id) {
        return articleRepository
                .findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    //ReadAll
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Transactional
    public Article update(Long id, ArticleUpdateRequest dto, Long userId) {

        /*
        toEntity() 필요없음. 이미 조회된 데이터가 Entity 임
         */
        // 1. target 엔티티 조회
        log.info("target 엔티티 조회");
        Article target = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        // 권한 체크
        if (!target.getMember().getId().equals(userId)) {
            throw new ForbiddenException();
        }

        // 2. 업데이트
        log.info("patch 메서드 실행");
        target.update(dto.getTitle(), dto.getContent());

        // save() 필요없음 - dirty checking
        return target;
    }


    //Delete
    @Transactional
    public void delete(Long id, Long userId) {
        Article target = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        if(!target.getMember().getId().equals(userId)) {
            throw new ForbiddenException();
        }

        commentRepository.deleteAllByArticleId(target.getId());
        articleRepository.deleteById(target.getId());
        log.info(id + "번 게시글 삭제성공");
    }
}
