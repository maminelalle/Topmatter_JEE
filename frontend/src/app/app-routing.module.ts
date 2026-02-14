import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { AuthComponent } from './auth/auth.component';
import { MainLayoutComponent } from './shared/main-layout/main-layout.component';
import { HomeComponent } from './posts/home/home.component';
import { FriendsComponent } from './friends/friends.component';
import { MessagesComponent } from './messages/messages.component';
import { ChatComponent } from './messages/chat/chat.component';
import { GroupsComponent } from './groups/groups.component';

const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: HomeComponent },
      { path: 'friends', component: FriendsComponent },
      { path: 'groups', component: GroupsComponent },
      { path: 'messages', component: MessagesComponent },
      { path: 'messages/chat/:userId', component: ChatComponent },
    ],
  },
  { path: '**', redirectTo: 'home' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
