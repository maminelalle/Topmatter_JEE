export interface Notification {
  id: number;
  type: string;
  message: string;
  actorId?: number;
  postId?: number;
  read: boolean;
  createdAt: string;
}
