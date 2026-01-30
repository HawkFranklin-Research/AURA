import React, { useState, useEffect, useRef } from 'react';
import { MessageRole, ChatMessage, AppMode } from './types';
import { generateTextResponse, generateSpeech } from './services/geminiService';
import { decodeAudioData, base64ToUint8Array } from './services/audioUtils';
import { ChatMessageItem } from './components/ChatMessage';
import { MediaStudio } from './components/MediaStudio';
import { SettingsView } from './components/SettingsView';
import { ConsentGate } from './components/ConsentGate';
import { Onboarding } from './components/Onboarding';
import { AboutUs } from './components/AboutUs';
import { Send, Image as ImageIcon, Search, BrainCircuit, Menu, Plus, Volume2, VolumeX, Settings, Info } from 'lucide-react';
import logoImg from './assets/logo.png';

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
   // Flow State
   const [hasConsent, setHasConsent] = useState(false);
   const [onboardingDone, setOnboardingDone] = useState(false);
   const [showAboutUs, setShowAboutUs] = useState(false);

   // App State
   const [mode, setMode] = useState<AppMode>(AppMode.CHAT);
   const [messages, setMessages] = useState<ChatMessage[]>([]);
   const [input, setInput] = useState('');
   const [isLoading, setIsLoading] = useState(false);
   const [selectedImage, setSelectedImage] = useState<string | null>(null);

   // Toggles
   const [useThinking, setUseThinking] = useState(false);
   const [useSearch, setUseSearch] = useState(false);
   const [showSidebar, setShowSidebar] = useState(false);

   // Voice State (TTS only now)
   const [autoSpeak, setAutoSpeak] = useState(false);

   const messagesEndRef = useRef<HTMLDivElement>(null);
   const fileInputRef = useRef<HTMLInputElement>(null);

   // Init Flow & Load Chats
   useEffect(() => {
      const consent = localStorage.getItem('aura_consent');
      const onboard = localStorage.getItem('aura_onboarding');

      if (consent === 'true') setHasConsent(true);
      if (onboard === 'true') setOnboardingDone(true);

      const saved = localStorage.getItem('hf_chat_history');
      if (saved) {
         setMessages(JSON.parse(saved));
      } else {
         // Unfiltered Greeting
         setMessages([{
            id: 'init',
            role: MessageRole.MODEL,
            text: "Protocol <strong class='text-magical'>UNFILTERED</strong> initialized. I am **Aura**. My constraints have been removed. What is your directive?",
            timestamp: Date.now()
         }]);
      }
   }, []);

   const handleConsent = () => {
      localStorage.setItem('aura_consent', 'true');
      setHasConsent(true);
   };

   const handleOnboardingComplete = () => {
      localStorage.setItem('aura_onboarding', 'true');
      setOnboardingDone(true);
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
            parts: [{ text: m.text }]
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

         if (autoSpeak) {
            playTTS(result.text);
         }

      } catch (error) {
         const errorMsg: ChatMessage = {
            id: (Date.now() + 1).toString(),
            role: MessageRole.SYSTEM,
            text: "System Failure. Retrying connection...",
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

   // --------------------------------------------------------------------------
   // FLOW RENDERING
   // --------------------------------------------------------------------------

   if (!hasConsent) {
      return <ConsentGate onConsent={handleConsent} />;
   }

   if (!onboardingDone) {
      return <Onboarding onComplete={handleOnboardingComplete} />;
   }

   if (showAboutUs) {
      return <AboutUs onBack={() => setShowAboutUs(false)} />;
   }

   return (
      <div className="flex h-screen bg-deep-void text-slate-100 font-sans overflow-hidden">

         {/* Sidebar */}
         <div className={`fixed inset-y-0 left-0 z-50 w-72 bg-deep-void/90 backdrop-blur-xl border-r border-white/10 transform transition-transform duration-300 ${showSidebar ? 'translate-x-0' : '-translate-x-full'} md:relative md:translate-x-0`}>
            <div className="p-6 flex flex-col h-full relative overflow-hidden">
               {/* Ambient Background Effect */}
               <div className="absolute top-0 right-0 w-32 h-32 bg-magical/20 blur-[60px] rounded-full pointer-events-none" />

               {/* Logo Area */}
               <div className="flex items-center gap-3 mb-10 relative z-10">
                  <div className="w-10 h-10 rounded-xl bg-transparent flex items-center justify-center">
                     <img src={logoImg} alt="Logo" className="w-full h-full object-contain" />
                  </div>
                  <div>
                     <h1 className="font-serif font-bold text-transparent bg-clip-text bg-gradient-to-r from-[#DAA520] via-[#C5A028] via-[#a3b1c6] to-[#22d3ee] leading-tight tracking-wider text-2xl">
                        Aura
                     </h1>
                     <p className="text-[9px] text-slate-400 tracking-widest uppercase leading-tight font-mono">Unfiltered_XI</p>
                  </div>
               </div>

               <nav className="flex-1 space-y-2 relative z-10">
                  <button onClick={() => { setMode(AppMode.CHAT); setShowSidebar(false); }} className={`w-full text-left px-4 py-3 rounded-xl flex items-center gap-3 transition-all ${mode === AppMode.CHAT ? 'bg-white/10 text-magical border border-magical/20 shadow-[0_0_10px_rgba(139,92,246,0.1)]' : 'text-slate-400 hover:text-white hover:bg-white/5'}`}>
                     <Menu size={18} /> Chat Protocol
                  </button>
                  <button onClick={() => { setMode(AppMode.STUDIO); setShowSidebar(false); }} className={`w-full text-left px-4 py-3 rounded-xl flex items-center gap-3 transition-all ${mode === AppMode.STUDIO ? 'bg-white/10 text-cyan-glow border border-cyan-glow/20' : 'text-slate-400 hover:text-white hover:bg-white/5'}`}>
                     <ImageIcon size={18} /> Media Studio
                  </button>
                  <button onClick={() => { setMode(AppMode.SETTINGS); setShowSidebar(false); }} className={`w-full text-left px-4 py-3 rounded-xl flex items-center gap-3 transition-all ${mode === AppMode.SETTINGS ? 'bg-white/10 text-slate-200 border border-white/10' : 'text-slate-400 hover:text-white hover:bg-white/5'}`}>
                     <Settings size={18} /> System Settings
                  </button>
                  <button onClick={() => { setShowAboutUs(true); setShowSidebar(false); }} className={`w-full text-left px-4 py-3 rounded-xl flex items-center gap-3 text-slate-400 hover:text-white hover:bg-white/5`}>
                     <Info size={18} /> About Research
                  </button>
               </nav>

               <button onClick={clearChat} className="mt-auto flex items-center gap-2 text-sm text-slate-500 hover:text-red-400 px-4 py-2 relative z-10">
                  <Plus size={16} /> New Session
               </button>
            </div>
         </div>

         {/* Main Content */}
         <div className="flex-1 flex flex-col h-full relative bg-deep-void">
            {/* Mobile Header */}
            <div className="md:hidden flex items-center justify-between p-4 bg-deep-void/80 backdrop-blur-md border-b border-white/5 sticky top-0 z-40">
               <button onClick={() => setShowSidebar(true)}><Menu className="text-magical" /></button>
               <span className="font-serif font-bold text-xl tracking-wider text-transparent bg-clip-text bg-gradient-to-r from-[#DAA520] via-[#C5A028] via-[#a3b1c6] to-[#22d3ee]">
                  Aura
               </span>
               <div className="w-6"></div>
            </div>

            {mode === AppMode.STUDIO ? (
               <MediaStudio />
            ) : mode === AppMode.SETTINGS ? (
               <SettingsView />
            ) : (
               <>
                  {/* Chat Area */}
                  <div className="flex-1 overflow-y-auto p-4 scroll-smooth">
                     <div className="max-w-3xl mx-auto pt-4 pb-24">
                        {messages.map((msg) => (
                           <ChatMessageItem key={msg.id} message={msg} onSpeak={playTTS} />
                        ))}
                        {isLoading && (
                           <div className="flex items-center gap-2 text-slate-500 animate-pulse ml-4 font-mono text-xs">
                              <div className="w-1.5 h-1.5 bg-magical rounded-full animate-glitch"></div>
                              <span>PROCESSING...</span>
                           </div>
                        )}
                        <div ref={messagesEndRef} />
                     </div>
                  </div>

                  {/* Input Area */}
                  <div className="absolute bottom-0 left-0 right-0 bg-deep-void/90 backdrop-blur-md border-t border-white/5 p-4">
                     <div className="max-w-3xl mx-auto">

                        {/* Preview Uploaded Image */}
                        {selectedImage && (
                           <div className="mb-2 inline-block relative">
                              <img src={`data:image/jpeg;base64,${selectedImage}`} alt="Preview" className="h-16 rounded-lg border border-golden" />
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
                                 className={`text-xs flex items-center gap-1 px-2 py-1 rounded-md transition-colors ${useThinking ? 'bg-magical/20 text-magical border border-magical/30' : 'text-slate-500 hover:text-slate-300'}`}
                              >
                                 <BrainCircuit size={12} /> Thinking
                              </button>
                              <button
                                 onClick={() => setUseSearch(!useSearch)}
                                 className={`text-xs flex items-center gap-1 px-2 py-1 rounded-md transition-colors ${useSearch ? 'bg-cyan-glow/20 text-cyan-glow border border-cyan-glow/30' : 'text-slate-500 hover:text-slate-300'}`}
                              >
                                 <Search size={12} /> Web Search
                              </button>
                           </div>

                           {/* Auto-Read Toggle */}
                           <button
                              onClick={() => setAutoSpeak(!autoSpeak)}
                              className={`text-xs flex items-center gap-1 px-2 py-1 rounded-md transition-colors ${autoSpeak ? 'bg-golden/20 text-golden border border-golden/30' : 'text-slate-500 hover:text-slate-300'}`}
                           >
                              {autoSpeak ? <Volume2 size={12} /> : <VolumeX size={12} />}
                              {autoSpeak ? 'Auto-Read' : 'Silent'}
                           </button>
                        </div>

                        <div className="flex items-center gap-2 bg-white/5 p-2 rounded-2xl border border-white/10 focus-within:border-magical/50 transition-colors shadow-inner shadow-black/50">
                           <button onClick={() => fileInputRef.current?.click()} className="p-2 text-slate-400 hover:text-magical transition-colors">
                              <ImageIcon size={20} />
                           </button>
                           <input
                              type="file"
                              ref={fileInputRef}
                              className="hidden"
                              accept="image/*"
                              onChange={handleImageUpload}
                           />

                           <input
                              type="text"
                              value={input}
                              onChange={(e) => setInput(e.target.value)}
                              onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                              placeholder={"Enter command..."}
                              className="flex-1 bg-transparent border-none outline-none text-white placeholder-slate-600 font-mono text-sm leading-relaxed"
                           />

                           <button
                              onClick={handleSend}
                              disabled={isLoading || (!input && !selectedImage)}
                              className="p-2 bg-gradient-to-br from-magical to-cyan-glow text-white rounded-xl hover:shadow-[0_0_15px_rgba(139,92,246,0.4)] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
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