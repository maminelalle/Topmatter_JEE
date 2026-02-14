import { Component, Input, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Comment } from '../../models/comment.model';

@Component({
  selector: 'app-comment-list',
  templateUrl: './comment-list.component.html',
  styleUrls: ['./comment-list.component.css'],
})
export class CommentListComponent implements OnInit {
  @Input() postId!: number;
  comments: Comment[] = [];
  newComment = '';
  loading = true;

  constructor(private api: ApiService, public auth: AuthService) {}

  canDelete(c: Comment): boolean {
    return !!this.auth.currentUser && c.user?.id === this.auth.currentUser.id;
  }

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.api.getComments(this.postId).subscribe({
      next: (list) => {
        this.comments = list;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  addComment(): void {
    const content = this.newComment?.trim();
    if (!content) return;
    this.api.addComment(this.postId, content).subscribe({
      next: () => {
        this.newComment = '';
        this.loadComments();
      },
      error: (err) => {
        alert(err.error?.message || (err.status === 401 ? 'Session expirée.' : 'Erreur lors de l\'envoi du commentaire.'));
      },
    });
  }

  deleteComment(id: number): void {
    this.api.deleteComment(id).subscribe({
      next: () => (this.comments = this.comments.filter((c) => c.id !== id)),
    });
  }
}
