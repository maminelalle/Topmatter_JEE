import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';
import { User } from '../models/user.model';

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css'],
})
export class MessagesComponent implements OnInit {
  friends: User[] = [];
  loading = true;

  constructor(private api: ApiService, private router: Router) {}

  ngOnInit(): void {
    this.api.getConversations().subscribe({
      next: (list) => (this.friends = list),
      complete: () => (this.loading = false),
    });
  }

  openChat(userId: number): void {
    this.router.navigate(['/messages/chat', userId]);
  }
}
