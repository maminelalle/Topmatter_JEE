import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { Message } from '../../models/message.model';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
})
export class ChatComponent implements OnInit, OnDestroy {
  otherUser: User | null = null;
  messages: Message[] = [];
  newMessage = '';
  loading = true;
  otherUserId = 0;
  private refreshInterval: ReturnType<typeof setInterval> | null = null;
  private initialLoadDone = false;

  constructor(
    private route: ActivatedRoute,
    private api: ApiService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('userId');
    if (id) {
      this.otherUserId = +id;
      this.api.getUser(this.otherUserId).subscribe((u) => (this.otherUser = u));
      this.loadMessages();
      this.refreshInterval = setInterval(() => this.loadMessages(), 3000);
    }
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }

  loadMessages(): void {
    if (!this.otherUserId) return;
    if (!this.initialLoadDone) this.loading = true;
    this.api.getConversation(this.otherUserId, 0, 100).subscribe({
      next: (list) => {
        this.messages = (list || []).reverse();
        this.loading = false;
        this.initialLoadDone = true;
      },
      error: () => {
        this.loading = false;
        this.initialLoadDone = true;
      },
    });
  }

  send(): void {
    const content = this.newMessage?.trim();
    if (!content || !this.otherUserId) return;
    this.api.sendMessage(this.otherUserId, content).subscribe({
      next: (msg) => {
        this.messages = [...this.messages, msg];
        this.newMessage = '';
      },
      error: (err) => {
        alert(err.error?.message || err.status === 401 ? 'Session expirée.' : 'Erreur lors de l\'envoi.');
      },
    });
  }

  isFromMe(msg: Message): boolean {
    return msg.sender?.id === this.auth.currentUser?.id;
  }
}
