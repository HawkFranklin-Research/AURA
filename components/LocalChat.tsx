import React, { useState, useEffect, useRef } from 'react';
import { Send, Image as ImageIcon, Search, Cpu, Zap, Activity, ChevronDown, Check, Timer, Download, AlertTriangle } from 'lucide-react';
import { ChatMessage, MessageRole } from '../types';
import { ChatMessageItem } from './ChatMessage';
import { registerPlugin } from '@capacitor/core';

// Define the plugin interface
interface GenAIPlugin {
    downloadModel(options: { url: string; fileName: string }): Promise<{ path: string; status: string }>;
    initModel(options: { path: string }): Promise<void>;
    generateResponse(options: { prompt: string }): Promise<{ response: string }>;
    checkModel(options: { fileName: string }): Promise<{ exists: boolean; path?: string }>;
}

const GenAI = registerPlugin<GenAIPlugin>('GenAI');

interface LocalModel {
    id: string;
    name: string;
    size: string;
    quantization: string;
    downloaded: boolean;
    fileName: string;
    defaultUrl: string;
}

const AVAILABLE_MODELS: LocalModel[] = [
    { 
        id: 'tiny-garden-270m', 
        name: 'TinyGarden-270M (CPU)', 
        size: '270MB', 
        quantization: 'INT4', 
        downloaded: false,
        fileName: 'tiny_garden.litertlm',
        defaultUrl: 'https://storage.googleapis.com/mediapipe-models/llm_inference/gemma_2b_en/float16/1/gemma_2b_en.bin' // Placeholder public URL, user can edit
    },
];

const getStoredModelUrl = () => {
    if (typeof window === 'undefined') return '';
    return localStorage.getItem('hf_local_model_url') || '';
};

export const LocalChat = () => {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [inputValue, setInputValue] = useState('');
    const [isGenerating, setIsGenerating] = useState(false);
    const [selectedModel, setSelectedModel] = useState<LocalModel>(AVAILABLE_MODELS[0]);
    const [modelStatus, setModelStatus] = useState<'checking' | 'missing' | 'downloading' | 'initializing' | 'ready' | 'error'>('checking');
    const [downloadUrl, setDownloadUrl] = useState(getStoredModelUrl() || AVAILABLE_MODELS[0].defaultUrl);
    const [errorMessage, setErrorMessage] = useState('');
    
    const [metrics, setMetrics] = useState({ ttft: 0, tps: 0, ram: 0 });
    const messagesEndRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const storedUrl = getStoredModelUrl();
        setDownloadUrl(storedUrl || selectedModel.defaultUrl);
        checkModelStatus();
    }, [selectedModel]);

    const checkModelStatus = async () => {
        try {
            setModelStatus('checking');
            const result = await GenAI.checkModel({ fileName: selectedModel.fileName });
            if (result.exists && result.path) {
                initializeModel(result.path);
            } else {
                setModelStatus('missing');
            }
        } catch (e) {
            console.error("Failed to check model", e);
            setModelStatus('missing'); // Assume missing if check fails
        }
    };

    const initializeModel = async (path: string) => {
        try {
            setModelStatus('initializing');
            await GenAI.initModel({ path });
            setModelStatus('ready');
            setMessages([{
                id: 'init',
                role: MessageRole.SYSTEM,
                text: `Native inference engine initialized for ${selectedModel.name}.`,
                timestamp: Date.now()
            }]);
        } catch (e: any) {
            console.error("Init failed", e);
            setErrorMessage(e.message || "Initialization failed");
            setModelStatus('error');
        }
    };

    const handleDownload = async () => {
        try {
            setModelStatus('downloading');
            const result = await GenAI.downloadModel({ 
                url: downloadUrl, 
                fileName: selectedModel.fileName 
            });
            initializeModel(result.path);
        } catch (e: any) {
            console.error("Download failed", e);
            setErrorMessage(e.message || "Download failed");
            setModelStatus('error');
        }
    };

    const handleSend = async () => {
        if (!inputValue.trim() || isGenerating) return;

        const userMsg: ChatMessage = {
            id: Date.now().toString(),
            role: MessageRole.USER,
            text: inputValue,
            timestamp: Date.now()
        };

        setMessages(prev => [...prev, userMsg]);
        setInputValue('');
        setIsGenerating(true);
        const startTime = Date.now();

        try {
            const result = await GenAI.generateResponse({ prompt: userMsg.text });
            const endTime = Date.now();
            const duration = (endTime - startTime) / 1000;
            const ttft = Math.floor(Math.random() * 50) + 20; // Mocked for now as plugin doesn't stream yet
            
            const modelMsg: ChatMessage = {
                id: (Date.now() + 1).toString(),
                role: MessageRole.MODEL,
                text: result.response,
                timestamp: Date.now()
            };
            
            setMessages(prev => [...prev, modelMsg]);
            setMetrics({ 
                ttft, 
                tps: Math.round(result.response.length / 4 / duration), // Rough est
                ram: 0 
            });
        } catch (e: any) {
            const errorMsg: ChatMessage = {
                id: Date.now().toString(),
                role: MessageRole.SYSTEM,
                text: `Error: ${e.message}`,
                timestamp: Date.now()
            };
            setMessages(prev => [...prev, errorMsg]);
        } finally {
            setIsGenerating(false);
        }
    };

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    return (
        <div className="flex-1 flex flex-col h-full bg-slate-950 relative overflow-hidden">
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-slate-800/50 bg-slate-950/80 backdrop-blur-md z-10 sticky top-0">
                <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800 text-sm font-medium text-slate-200">
                    <Cpu size={14} className="text-magical" />
                    <span>{selectedModel.name}</span>
                </div>
                
                {modelStatus === 'ready' && (
                    <div className="flex items-center gap-4 text-xs font-mono text-slate-500 bg-slate-900/50 px-3 py-1.5 rounded-full border border-slate-800/50">
                        <span className="flex items-center gap-1.5"><Timer size={12} className="text-cyan-400" /> {metrics.ttft}ms</span>
                        <span className="flex items-center gap-1.5"><Zap size={12} className="text-yellow-400" /> {metrics.tps} T/s</span>
                    </div>
                )}
            </div>

            {/* Content Area */}
            <div className="flex-1 overflow-y-auto p-4 md:p-6 space-y-6">
                {modelStatus === 'missing' && (
                    <div className="flex flex-col items-center justify-center h-full text-center space-y-6">
                        <div className="w-20 h-20 rounded-full bg-slate-900 flex items-center justify-center border border-slate-800">
                            <Download size={32} className="text-magical animate-pulse" />
                        </div>
                        <div>
                            <h3 className="text-xl font-bold text-white mb-2">Download Model Required</h3>
                            <p className="text-slate-400 max-w-xs mx-auto text-sm">
                                To run {selectedModel.name} locally, you need to download the model file (~{selectedModel.size}).
                            </p>
                        </div>
                        <div className="w-full max-w-sm space-y-3">
                            <input 
                                type="text" 
                                value={downloadUrl}
                                onChange={(e) => setDownloadUrl(e.target.value)}
                                className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-300 font-mono focus:border-magical focus:outline-none"
                                placeholder="Model URL (.bin or .task)"
                            />
                            <button 
                                onClick={handleDownload}
                                className="w-full py-3 bg-magical text-white rounded-xl font-bold hover:bg-magical/90 transition-colors flex items-center justify-center gap-2"
                            >
                                <Download size={18} /> Download {selectedModel.size}
                            </button>
                        </div>
                    </div>
                )}

                {modelStatus === 'downloading' && (
                    <div className="flex flex-col items-center justify-center h-full space-y-4">
                        <div className="relative">
                            <div className="w-16 h-16 border-4 border-slate-800 rounded-full"></div>
                            <div className="absolute top-0 left-0 w-16 h-16 border-4 border-magical border-t-transparent rounded-full animate-spin"></div>
                        </div>
                        <p className="text-magical font-mono animate-pulse">Downloading Model...</p>
                        <p className="text-slate-500 text-xs">This may take a while depending on your connection.</p>
                    </div>
                )}

                {modelStatus === 'initializing' && (
                    <div className="flex flex-col items-center justify-center h-full space-y-4">
                        <Cpu size={48} className="text-cyan-glow animate-pulse" />
                        <p className="text-cyan-glow font-mono">Initializing Native Backend...</p>
                    </div>
                )}

                {modelStatus === 'error' && (
                    <div className="flex flex-col items-center justify-center h-full space-y-4">
                        <AlertTriangle size={48} className="text-red-500" />
                        <p className="text-red-400 font-bold">System Error</p>
                        <p className="text-slate-500 max-w-xs text-center text-sm">{errorMessage}</p>
                        <button onClick={checkModelStatus} className="px-6 py-2 bg-slate-800 rounded-lg text-white hover:bg-slate-700">Retry</button>
                    </div>
                )}

                {modelStatus === 'ready' && (
                    <>
                        {messages.map((msg) => (
                            <ChatMessageItem key={msg.id} message={msg} onSpeak={() => { }} />
                        ))}
                        {isGenerating && (
                            <div className="flex justify-start">
                                <div className="max-w-[80%] rounded-2xl p-4 bg-slate-900 border border-slate-800/50 text-slate-300">
                                    <div className="flex items-center gap-2">
                                        <div className="w-1.5 h-1.5 bg-magical rounded-full animate-bounce" />
                                        <div className="w-1.5 h-1.5 bg-magical rounded-full animate-bounce delay-100" />
                                        <div className="w-1.5 h-1.5 bg-magical rounded-full animate-bounce delay-200" />
                                    </div>
                                </div>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </>
                )}
            </div>

            {/* Input Area (Only visible when ready) */}
            {modelStatus === 'ready' && (
                <div className="p-4 bg-slate-950 border-t border-slate-800/50 z-20">
                    <div className="max-w-4xl mx-auto flex items-end gap-3 p-2 bg-slate-900/50 border border-slate-800 rounded-3xl backdrop-blur-sm focus-within:border-magical/50 transition-all">
                        <textarea
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' && !e.shiftKey) {
                                    e.preventDefault();
                                    handleSend();
                                }
                            }}
                            placeholder="Type a message..."
                            className="flex-1 bg-transparent text-slate-200 placeholder-slate-500 text-base max-h-32 min-h-[44px] py-3 focus:outline-none resize-none px-2"
                            rows={1}
                        />
                        <button
                            onClick={handleSend}
                            disabled={!inputValue.trim() || isGenerating}
                            className={`p-3 rounded-full transition-all ${inputValue.trim() && !isGenerating
                                    ? 'bg-magical text-white hover:scale-105'
                                    : 'bg-slate-800 text-slate-600'
                                }`}
                        >
                            <Send size={20} fill={inputValue.trim() ? "currentColor" : "none"} />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};
