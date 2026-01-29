import React, { useState, useEffect, useRef } from 'react';
import { MessageRole, ChatMessage, AppMode } from './types';
import { generateTextResponse, generateSpeech } from './services/geminiService';
import { decodeAudioData, base64ToUint8Array } from './services/audioUtils';
import { ChatMessageItem } from './components/ChatMessage';
import { LiveSession } from './components/LiveSession';
import { MediaStudio } from './components/MediaStudio';
import { Send, Mic, Image as ImageIcon, Search, BrainCircuit, Menu, Plus, Volume2, VolumeX } from 'lucide-react';

// Custom Type for Window AI Studio
declare global {
  interface AIStudio {
    hasSelectedApiKey: () => Promise<boolean>;
    openSelectKey: () => Promise<void>;
  }

  interface Window {
    aistudio?: AIStudio;
    webkitSpeechRecognition?: any;
    SpeechRecognition?: any;
  }
}

export default function App() {
  const [mode, setMode] = useState<AppMode>(AppMode.CHAT);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  
  // Toggles
  const [useThinking, setUseThinking] = useState(false);
  const [useSearch, setUseSearch] = useState(false);
  const [showSidebar, setShowSidebar] = useState(false);
  
  // Voice State
  const [isListening, setIsListening] = useState(false);
  const [autoSpeak, setAutoSpeak] = useState(false);
  const recognitionRef = useRef<any>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Load chats on mount
  useEffect(() => {
    const saved = localStorage.getItem('hf_chat_history');
    if (saved) {
      setMessages(JSON.parse(saved));
    } else {
        // Initial Greeting
        setMessages([{
            id: 'init',
            role: MessageRole.MODEL,
            text: "Greetings. I am **Aura**, your personal research assistant by HawkFranklin. I can see, hear, and speak. How can I help you achieve your goals today?",
            timestamp: Date.now()
        }]);
    }
  }, []);

  // Initialize Speech Recognition
  useEffect(() => {
    if (window.webkitSpeechRecognition || window.SpeechRecognition) {
      const SpeechRecognition = window.webkitSpeechRecognition || window.SpeechRecognition;
      recognitionRef.current = new SpeechRecognition();
      recognitionRef.current.continuous = false;
      recognitionRef.current.interimResults = true;
      recognitionRef.current.lang = 'en-US';

      recognitionRef.current.onresult = (event: any) => {
        const transcript = Array.from(event.results)
          .map((result: any) => result[0])
          .map((result) => result.transcript)
          .join('');
        setInput(transcript);
      };

      recognitionRef.current.onend = () => {
        setIsListening(false);
      };
      
      recognitionRef.current.onerror = (event: any) => {
        console.error("Speech Recognition Error", event.error);
        setIsListening(false);
      };
    }
  }, []);

  const toggleMic = () => {
    if (!recognitionRef.current) {
        alert("Voice input is not supported in this browser.");
        return;
    }
    if (isListening) {
      recognitionRef.current.stop();
      setIsListening(false);
    } else {
      recognitionRef.current.start();
      setIsListening(true);
      // Auto-enable speaker if user uses voice
      setAutoSpeak(true);
    }
  };

  // Save chats
  useEffect(() => {
    if (messages.length > 0) {
      localStorage.setItem('hf_chat_history', JSON.stringify(messages));
    }
  }, [messages]);

  // Scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  const handleSend = async () => {
    if ((!input.trim() && !selectedImage) || isLoading) return;

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      role: MessageRole.USER,
      text: input,
      image: selectedImage || undefined,
      timestamp: Date.now()
    };

    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setSelectedImage(null);
    setIsLoading(true);

    try {
      // Prepare history for API
      const history = messages.map(m => ({
         role: m.role,
         parts: [{ text: m.text }] // Simplified history
      }));

      const result = await generateTextResponse(history, userMsg.text, userMsg.image, useThinking, useSearch);

      const botMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        role: MessageRole.MODEL,
        text: result.text,
        timestamp: Date.now(),
        groundingUrls: result.groundingUrls
      };

      setMessages(prev => [...prev, botMsg]);

      // Auto Speak Response
      if (autoSpeak) {
        playTTS(result.text);
      }

    } catch (error) {
      const errorMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        role: MessageRole.SYSTEM,
        text: "An error occurred while processing your request. Please try again.",
        timestamp: Date.now()
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
     const file = e.target.files?.[0];
     if (file) {
        const reader = new FileReader();
        reader.onloadend = () => {
           // Remove data URL prefix for API
           const base64 = (reader.result as string).split(',')[1];
           setSelectedImage(base64);
        };
        reader.readAsDataURL(file);
     }
  };

  const playTTS = async (text: string) => {
      try {
          const base64Audio = await generateSpeech(text);
          const ctx = new (window.AudioContext || (window as any).webkitAudioContext)();
          const buffer = await decodeAudioData(base64ToUint8Array(base64Audio), ctx);
          const source = ctx.createBufferSource();
          source.buffer = buffer;
          source.connect(ctx.destination);
          source.start();
      } catch (e) {
          console.error("TTS Failed", e);
      }
  };

  const clearChat = () => {
      setMessages([]);
      localStorage.removeItem('hf_chat_history');
      setShowSidebar(false);
  };

  if (mode === AppMode.LIVE) {
    return <LiveSession onClose={() => setMode(AppMode.CHAT)} />;
  }

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 font-sans overflow-hidden">
      
      {/* Sidebar (Mobile/Desktop) */}
      <div className={`fixed inset-y-0 left-0 z-50 w-64 bg-slate-900 border-r border-slate-800 transform transition-transform duration-300 ${showSidebar ? 'translate-x-0' : '-translate-x-full'} md:relative md:translate-x-0`}>
         <div className="p-6 flex flex-col h-full">
            {/* Logo Area */}
            <div className="flex items-center gap-3 mb-8">
               <div className="w-10 h-10 rounded-full bg-gradient-to-br from-yellow-500 to-yellow-700 flex items-center justify-center font-bold text-slate-900 text-lg">A</div>
               <div>
                  <h1 className="font-serif font-bold text-yellow-500 leading-tight">Aura</h1>
                  <p className="text-[10px] text-slate-400 tracking-wide uppercase">By HawkFranklin</p>
               </div>
            </div>

            <nav className="flex-1 space-y-2">
               <button onClick={() => { setMode(AppMode.CHAT); setShowSidebar(false); }} className={`w-full text-left px-4 py-3 rounded-xl flex items-center gap-3 ${mode === AppMode.CHAT ? 'bg-slate-800 text-yellow-500' : 'text-slate-400 hover:text-slate-200'}`}>
                  <Menu size={18} /> Chat
               </button>
               <button onClick={() => { setMode(AppMode.STUDIO); setShowSidebar(false); }} className={`w-full text-left px-4 py-3 rounded-xl flex items-center gap-3 ${mode === AppMode.STUDIO ? 'bg-slate-800 text-yellow-500' : 'text-slate-400 hover:text-slate-200'}`}>
                  <ImageIcon size={18} /> Studio
               </button>
               <button onClick={() => { setMode(AppMode.LIVE); setShowSidebar(false); }} className="w-full text-left px-4 py-3 rounded-xl flex items-center gap-3 text-slate-400 hover:text-slate-200 hover:bg-slate-800">
                  <Mic size={18} /> Live Voice
               </button>
            </nav>
            
            <button onClick={clearChat} className="mt-auto flex items-center gap-2 text-sm text-slate-500 hover:text-red-400 px-4 py-2">
               <Plus size={16} /> New Chat Session
            </button>
         </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col h-full relative">
         {/* Mobile Header */}
         <div className="md:hidden flex items-center justify-between p-4 bg-slate-900 border-b border-slate-800">
            <button onClick={() => setShowSidebar(true)}><Menu className="text-yellow-500" /></button>
            <span className="font-serif font-bold text-yellow-500">Aura</span>
            <div className="w-6"></div>
         </div>

         {mode === AppMode.STUDIO ? (
            <MediaStudio />
         ) : (
            <>
               {/* Chat Area */}
               <div className="flex-1 overflow-y-auto p-4 scroll-smooth">
                  <div className="max-w-3xl mx-auto pt-4 pb-24">
                     {messages.map((msg) => (
                        <ChatMessageItem key={msg.id} message={msg} onSpeak={playTTS} />
                     ))}
                     {isLoading && (
                        <div className="flex items-center gap-2 text-slate-500 animate-pulse ml-4">
                           <div className="w-2 h-2 bg-yellow-600 rounded-full animate-bounce"></div>
                           <div className="w-2 h-2 bg-yellow-600 rounded-full animate-bounce delay-100"></div>
                           <div className="w-2 h-2 bg-yellow-600 rounded-full animate-bounce delay-200"></div>
                        </div>
                     )}
                     <div ref={messagesEndRef} />
                  </div>
               </div>

               {/* Input Area */}
               <div className="absolute bottom-0 left-0 right-0 bg-slate-900/90 backdrop-blur-md border-t border-slate-800 p-4">
                  <div className="max-w-3xl mx-auto">
                     
                     {/* Preview Uploaded Image */}
                     {selectedImage && (
                        <div className="mb-2 inline-block relative">
                           <img src={`data:image/jpeg;base64,${selectedImage}`} alt="Preview" className="h-16 rounded-lg border border-yellow-600" />
                           <button onClick={() => setSelectedImage(null)} className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-0.5">
                              <Plus className="rotate-45" size={12} />
                           </button>
                        </div>
                     )}

                     {/* Toolbar */}
                     <div className="flex items-center justify-between mb-2 px-2">
                        <div className="flex items-center gap-3">
                            <button 
                            onClick={() => setUseThinking(!useThinking)}
                            className={`text-xs flex items-center gap-1 px-2 py-1 rounded-md transition-colors ${useThinking ? 'bg-purple-900/50 text-purple-300 border border-purple-700' : 'text-slate-500 hover:text-slate-300'}`}
                            >
                            <BrainCircuit size={12} /> Thinking
                            </button>
                            <button 
                            onClick={() => setUseSearch(!useSearch)}
                            className={`text-xs flex items-center gap-1 px-2 py-1 rounded-md transition-colors ${useSearch ? 'bg-blue-900/50 text-blue-300 border border-blue-700' : 'text-slate-500 hover:text-slate-300'}`}
                            >
                            <Search size={12} /> Web Search
                            </button>
                        </div>
                        
                        {/* Auto-Read Toggle */}
                        <button 
                           onClick={() => setAutoSpeak(!autoSpeak)}
                           className={`text-xs flex items-center gap-1 px-2 py-1 rounded-md transition-colors ${autoSpeak ? 'bg-yellow-900/50 text-yellow-400 border border-yellow-700' : 'text-slate-500 hover:text-slate-300'}`}
                        >
                           {autoSpeak ? <Volume2 size={12} /> : <VolumeX size={12} />}
                           {autoSpeak ? 'Auto-Read On' : 'Auto-Read Off'}
                        </button>
                     </div>

                     <div className="flex items-center gap-2 bg-slate-800 p-2 rounded-2xl border border-slate-700 focus-within:border-yellow-600/50 transition-colors">
                        <button onClick={() => fileInputRef.current?.click()} className="p-2 text-slate-400 hover:text-yellow-500 transition-colors">
                           <ImageIcon size={20} />
                        </button>
                        <input 
                           type="file" 
                           ref={fileInputRef} 
                           className="hidden" 
                           accept="image/*" 
                           onChange={handleImageUpload} 
                        />
                        
                        {/* Mic Button */}
                        <button 
                            onClick={toggleMic}
                            className={`p-2 transition-colors ${isListening ? 'text-red-500 animate-pulse' : 'text-slate-400 hover:text-white'}`}
                        >
                            <Mic size={20} />
                        </button>

                        <input 
                           type="text" 
                           value={input}
                           onChange={(e) => setInput(e.target.value)}
                           onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                           placeholder={isListening ? "Listening..." : "Ask Aura..."}
                           className="flex-1 bg-transparent border-none outline-none text-white placeholder-slate-500"
                        />
                        
                        <button 
                           onClick={handleSend}
                           disabled={isLoading || (!input && !selectedImage)}
                           className="p-2 bg-yellow-600 text-white rounded-xl hover:bg-yellow-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                           <Send size={20} />
                        </button>
                     </div>
                  </div>
               </div>
            </>
         )}
      </div>
    </div>
  );
}