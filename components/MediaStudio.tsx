import React, { useState } from 'react';
import { Image as ImageIcon, Video, Loader2, Download, AlertTriangle } from 'lucide-react';
import { generateImage, generateVideo } from '../services/geminiService';

export const MediaStudio: React.FC = () => {
  const [mode, setMode] = useState<'image' | 'video'>('image');
  const [prompt, setPrompt] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [resultUrl, setResultUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleGenerate = async () => {
     if (!prompt) return;
     setIsLoading(true);
     setError(null);
     setResultUrl(null);

     try {
       let url;
       if (mode === 'image') {
          // Determine quality based on keywords or toggle (simplified to auto-detect "high quality" in prompt for demo)
          const isPro = prompt.toLowerCase().includes("hd") || prompt.toLowerCase().includes("4k");
          url = await generateImage(prompt, isPro);
       } else {
          url = await generateVideo(prompt);
       }
       setResultUrl(url);
     } catch (e: any) {
       setError(e.message || "Generation failed. For Video, ensure you selected a key.");
     } finally {
       setIsLoading(false);
     }
  };

  return (
    <div className="flex flex-col h-full bg-slate-900 p-4 overflow-y-auto">
       <div className="max-w-xl mx-auto w-full space-y-6">
          
          {/* Header */}
          <div className="text-center py-4">
             <h2 className="text-3xl font-serif text-yellow-500 tracking-wide">Aura Studio</h2>
             <p className="text-slate-400 text-sm">Create visuals with Aura by HawkFranklin</p>
          </div>

          {/* Mode Switcher */}
          <div className="flex p-1 bg-slate-800 rounded-xl">
             <button 
                onClick={() => setMode('image')}
                className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-lg text-sm font-medium transition-all ${mode === 'image' ? 'bg-yellow-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'}`}
             >
                <ImageIcon size={18} /> Image
             </button>
             <button 
                onClick={() => setMode('video')}
                className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-lg text-sm font-medium transition-all ${mode === 'video' ? 'bg-yellow-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'}`}
             >
                <Video size={18} /> Video (Veo)
             </button>
          </div>

          {/* Input Area */}
          <div className="space-y-4">
             <textarea 
               value={prompt}
               onChange={(e) => setPrompt(e.target.value)}
               placeholder={`Describe the ${mode} you want to create...`}
               className="w-full bg-slate-800 border border-slate-700 rounded-xl p-4 text-white focus:ring-2 focus:ring-yellow-600 focus:border-transparent outline-none resize-none h-32"
             />
             
             {mode === 'video' && (
               <div className="flex items-start gap-2 text-xs text-yellow-600/80 bg-yellow-900/10 p-2 rounded">
                  <AlertTriangle size={14} className="mt-0.5" />
                  <p>Video generation (Veo) requires a paid billing project. You may be prompted to select a Google Cloud Project key.</p>
               </div>
             )}

             <button
               onClick={handleGenerate}
               disabled={isLoading || !prompt}
               className="w-full bg-gradient-to-r from-yellow-600 to-yellow-700 text-white font-bold py-4 rounded-xl shadow-lg hover:shadow-yellow-600/20 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
             >
                {isLoading ? <Loader2 className="animate-spin" /> : (mode === 'image' ? <ImageIcon /> : <Video />)}
                {isLoading ? 'Generating...' : `Generate ${mode === 'image' ? 'Image' : 'Video'}`}
             </button>
          </div>

          {/* Error */}
          {error && (
            <div className="bg-red-900/20 border border-red-800 text-red-200 p-4 rounded-xl text-sm text-center">
              {error}
            </div>
          )}

          {/* Result */}
          {resultUrl && (
             <div className="mt-8 bg-slate-800 p-2 rounded-2xl shadow-xl border border-slate-700">
                {mode === 'image' ? (
                   <img src={resultUrl} alt="Generated" className="w-full rounded-xl" />
                ) : (
                   <video src={resultUrl} controls className="w-full rounded-xl" autoPlay loop />
                )}
                <div className="flex justify-end p-2">
                   <a href={resultUrl} download={`aura-${Date.now()}.${mode === 'image' ? 'png' : 'mp4'}`} className="flex items-center gap-2 text-sm text-yellow-500 hover:text-yellow-400 font-medium">
                      <Download size={16} /> Save Asset
                   </a>
                </div>
             </div>
          )}

       </div>
    </div>
  );
};