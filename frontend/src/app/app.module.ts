import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthComponent } from './auth/auth.component';
import { MainLayoutComponent } from './shared/main-layout/main-layout.component';
import { HomeComponent } from './posts/home/home.component';
import { PostCardComponent } from './posts/post-card/post-card.component';
import { CommentListComponent } from './posts/comment-list/comment-list.component';
import { FriendsComponent } from './friends/friends.component';
import { MessagesComponent } from './messages/messages.component';
import { ChatComponent } from './messages/chat/chat.component';
import { GroupsComponent } from './groups/groups.component';
import { AuthInterceptor } from './interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    AuthComponent,
    MainLayoutComponent,
    HomeComponent,
    PostCardComponent,
    CommentListComponent,
    FriendsComponent,
    MessagesComponent,
    ChatComponent,
    GroupsComponent,
  ],
  imports: [
    BrowserModule,
    CommonModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
