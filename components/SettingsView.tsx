import React, { useState, useEffect } from 'react';
import { Save, Key, ExternalLink, ShieldCheck } from 'lucide-react';

export const SettingsView = () => {
  const [apiKey, setApiKey] = useState('');
  const [isSaved, setIsSaved] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('hf_gemini_api_key');
    if (stored) setApiKey(stored);
  }, []);

  const handleSave = () => {
    if (apiKey.trim()) {
      localStorage.setItem('hf_gemini_api_key', apiKey.trim());
      setIsSaved(true);
      setTimeout(() => setIsSaved(false), 2000);
    }
  };

  const handleClear = () => {
      localStorage.removeItem('hf_gemini_api_key');
      setApiKey('');
  };

  return (
    <div className="flex-1 overflow-y-auto p-4 md:p-8 bg-slate-950">
      <div className="max-w-2xl mx-auto space-y-8">
        <div>
          <h2 className="text-3xl font-serif font-bold text-yellow-500 mb-2">Settings</h2>
          <p className="text-slate-400">Configure your Aura experience.</p>
        </div>

        <div className="bg-slate-900 rounded-2xl p-6 border border-slate-800 space-y-6">
          <div className="flex items-center gap-3 text-white mb-4">
             <div className="p-2 bg-slate-800 rounded-lg text-yellow-500">
               <Key size={24} />
             </div>
             <h3 className="text-xl font-bold">API Configuration</h3>
          </div>

          <p className="text-sm text-slate-400">
            Aura requires a Google Gemini API key to function. Your key is stored locally on your device and is never sent to our servers.
          </p>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Gemini API Key</label>
            <input 
              type="password" 
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="AIzaSy..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-white focus:ring-2 focus:ring-yellow-600 outline-none transition-all placeholder-slate-600 font-mono"
            />
          </div>

          <div className="flex items-center gap-4">
            <button 
              onClick={handleSave}
              className={`flex items-center gap-2 px-6 py-3 rounded-xl font-bold transition-all ${isSaved ? 'bg-green-600 text-white' : 'bg-yellow-600 text-white hover:bg-yellow-700'}`}
            >
              {isSaved ? <ShieldCheck size={18} /> : <Save size={18} />}
              {isSaved ? 'Saved Securely' : 'Save Key'}
            </button>
            
            {apiKey && (
                <button onClick={handleClear} className="text-red-400 text-sm hover:text-red-300 px-4">
                    Clear Key
                </button>
            )}
          </div>
          
          <div className="pt-4 border-t border-slate-800">
             <a 
               href="https://aistudio.google.com/app/apikey" 
               target="_blank" 
               rel="noopener noreferrer"
               className="inline-flex items-center gap-2 text-sm text-blue-400 hover:text-blue-300"
             >
               <ExternalLink size={14} /> Get a free API Key from Google AI Studio
             </a>
          </div>
        </div>

        <div className="bg-slate-900 rounded-2xl p-6 border border-slate-800 opacity-50 pointer-events-none">
           <h3 className="text-lg font-bold text-white mb-2">Appearance (Coming Soon)</h3>
           <p className="text-slate-500 text-sm">Theme customization and custom avatars will be available in the next update.</p>
        </div>
      </div>
    </div>
  );
};
