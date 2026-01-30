export enum MessageRole {
  USER = 'user',
  MODEL = 'model',
  SYSTEM = 'system'
}

export enum AppMode {
  CHAT = 'chat',
  STUDIO = 'studio',
  SETTINGS = 'settings',
  LOCAL_CHAT = 'local_chat'
}

export interface ChatMessage {
  id: string;
  role: MessageRole;
  text: string;
  image?: string; // base64
  timestamp: number;
  isThinking?: boolean;
  groundingUrls?: Array<{ title: string, uri: string }>;
}

export interface ChatSession {
  id: string;
  title: string;
  messages: ChatMessage[];
  lastUpdated: number;
}

export interface MediaGenerationConfig {
  type: 'image' | 'video';
  prompt: string;
  aspectRatio: string;
  style?: string;
}



