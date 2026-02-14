export interface User {
  id: number;
  username: string;
  email: string;
  avatarUrl?: string;
  bio?: string;
  online?: boolean;
  lastSeen?: string;
  role?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  role: string;
}
