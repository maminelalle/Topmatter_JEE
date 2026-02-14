import { User } from './user.model';

export interface Message {
  id: number;
  content: string;
  sender: User;
  receiverId: number;
  read: boolean;
  createdAt: string;
}
