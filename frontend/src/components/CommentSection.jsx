import { useCallback, useEffect, useState } from 'react';
import { api } from '../api/client';
import { toPermissionMessage, useAuth } from '../api/auth';
import { formatDate } from './format';

export default function CommentSection({ articleId, onNotify, onConfirm }) {
  const { isLoggedIn, isOwner, checkPermission } = useAuth();
  const [comments, setComments] = useState([]);
  const [content, setContent] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editingText, setEditingText] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(() => {
    api
      .listComments(articleId)
      .then(setComments)
      .catch((err) => onNotify(err.message));
  }, [articleId, onNotify]);

  useEffect(load, [load]);

  const guard = (writer) => {
    const { ok, message } = checkPermission(writer);
    if (!ok) onNotify(message);
    return ok;
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!isLoggedIn) {
      onNotify('로그인 필요합니다');
      return;
    }
    if (!content.trim()) {
      onNotify('댓글 내용은 필수 입니다.');
      return;
    }
    setSubmitting(true);
    try {
      await api.createComment(articleId, { content });
      setContent('');
      load();
    } catch (err) {
      onNotify(toPermissionMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const startEdit = (comment) => {
    if (!guard(comment.writer)) return;
    setEditingId(comment.id);
    setEditingText(comment.content);
  };

  const submitEdit = async (commentId) => {
    if (!editingText.trim()) {
      onNotify('댓글 내용은 필수 입니다.');
      return;
    }
    try {
      await api.updateComment(commentId, { content: editingText });
      setEditingId(null);
      load();
    } catch (err) {
      onNotify(toPermissionMessage(err));
    }
  };

  const handleDelete = (comment) => {
    if (!guard(comment.writer)) return;
    onConfirm({
      message: '이 댓글을 삭제할까요?',
      onConfirm: async () => {
        onConfirm(null);
        try {
          await api.deleteComment(comment.id);
          load();
        } catch (err) {
          onNotify(toPermissionMessage(err));
        }
      },
    });
  };

  return (
    <section className="comments">
      <h3>댓글 {comments.length}</h3>

      {comments.map((c) => (
        <div className="comment" key={c.id}>
          <div className="comment-head">
            <span className="comment-writer">{c.writer}</span>
            <span className="comment-date">{formatDate(c.createdAt)}</span>
            <span className="comment-links">
              {editingId === c.id ? (
                <>
                  <button className="link-action" onClick={() => submitEdit(c.id)}>
                    저장
                  </button>
                  <button className="link-action" onClick={() => setEditingId(null)}>
                    취소
                  </button>
                </>
              ) : (
                /* 본인 댓글일 때만 수정/삭제 노출 */
                isOwner(c.writer) && (
                  <>
                    <button className="link-action" onClick={() => startEdit(c)}>
                      수정
                    </button>
                    <button className="link-action danger" onClick={() => handleDelete(c)}>
                      삭제
                    </button>
                  </>
                )
              )}
            </span>
          </div>

          {editingId === c.id ? (
            <textarea
              rows={3}
              value={editingText}
              onChange={(e) => setEditingText(e.target.value)}
              style={{ marginTop: 8 }}

            />
          ) : (
            <div className="comment-body">{c.content}</div>
          )}
        </div>
      ))}

      <form className="comment-form" onSubmit={handleCreate}>
        <textarea
          rows={8}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder={isLoggedIn ? '댓글을 입력하세요' : '로그인 후 댓글을 작성할 수 있습니다'}
        />
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          등록
        </button>
      </form>
    </section>
  );
}
