import React from 'react';
import ReactMarkdown from 'react-markdown';
import { ChatMessage, MessageRole } from '../types';
import { Copy, Volume2 } from 'lucide-react';

interface Props {
  message: ChatMessage;
  onSpeak: (text: string) => void;
}

export const ChatMessageItem: React.FC<Props> = ({ message, onSpeak }) => {
  const isUser = message.role === MessageRole.USER;

  return (
    <div className={`flex w-full mb-6 ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div className={`max-w-[85%] sm:max-w-[75%] flex flex-col ${isUser ? 'items-end' : 'items-start'}`}>

        <div className={`px-4 py-3 rounded-2xl shadow-lg relative group transition-all duration-300 ${isUser
            ? 'bg-gradient-to-br from-cyan-glow/20 to-cyan-700/50 text-white rounded-br-none border border-cyan-glow/30 shadow-[0_0_10px_rgba(34,211,238,0.2)]'
            : 'bg-white/5 text-slate-100 rounded-bl-none border border-magical/20 shadow-[0_0_15px_rgba(139,92,246,0.1)] backdrop-blur-sm'
          }`}>
          {message.image && (
            <img
              src={message.image}
              alt="User upload"
              className="mb-3 rounded-lg max-h-64 object-cover border border-cyan-glow/50"
            />
          )}

          <div className="prose prose-invert prose-sm max-w-none">
            {message.isThinking ? (
              <div className="flex items-center gap-2 text-magical italic animate-pulse font-mono text-xs">
                <span className="w-2 h-2 bg-magical rounded-full animate-glitch"></span>
                <span className="tracking-widest">PROCESSING_THOUGHTS...</span>
              </div>
            ) : (
              <ReactMarkdown components={{
                strong: ({ node, ...props }) => <span className="text-transparent bg-clip-text bg-gradient-to-r from-magical to-cyan-glow font-bold" {...props} />,
                a: ({ node, ...props }) => <a className="text-cyan-glow underline decoration-cyan-glow/30 hover:decoration-cyan-glow hover:text-white transition-all" {...props} />
              }}>{message.text}</ReactMarkdown>
            )}
          </div>

          {!isUser && !message.isThinking && (
            <div className="absolute -bottom-8 left-0 hidden group-hover:flex gap-2">
              <button
                onClick={() => onSpeak(message.text)}
                className="p-1.5 bg-white/10 border border-white/10 rounded-full hover:bg-magical hover:border-magical transition-colors text-white"
              >
                <Volume2 size={14} />
              </button>
              <button
                onClick={() => navigator.clipboard.writeText(message.text)}
                className="p-1.5 bg-white/10 border border-white/10 rounded-full hover:bg-cyan-glow hover:border-cyan-glow transition-colors text-white"
              >
                <Copy size={14} />
              </button>
            </div>
          )}
        </div>

        {/* Grounding Sources */}
        {!isUser && message.groundingUrls && message.groundingUrls.length > 0 && (
          <div className="mt-2 text-xs text-slate-400 flex flex-wrap gap-2">
            <span className="font-semibold text-yellow-600">Sources:</span>
            {message.groundingUrls.map((url, i) => (
              <a
                key={i}
                href={url.uri}
                target="_blank"
                rel="noreferrer"
                className="hover:text-yellow-400 underline decoration-slate-600"
              >
                {url.title}
              </a>
            ))}
          </div>
        )}

        <span className="text-[10px] text-slate-500 mt-1 px-1">
          {new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
        </span>
      </div>
    </div>
  );
};