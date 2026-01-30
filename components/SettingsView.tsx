import React, { useState, useEffect } from 'react';
import { Save, Key, ExternalLink, ShieldCheck, Download, Cpu, GitBranch } from 'lucide-react';

export const SettingsView = () => {
  const [apiKey, setApiKey] = useState('');
  const [isSaved, setIsSaved] = useState(false);
  const [modelUrl, setModelUrl] = useState('');
  const [isModelUrlSaved, setIsModelUrlSaved] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('hf_gemini_api_key');
    if (stored) setApiKey(stored);
    const storedModelUrl = localStorage.getItem('hf_local_model_url');
    if (storedModelUrl) setModelUrl(storedModelUrl);
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

  const handleModelUrlSave = () => {
    if (modelUrl.trim()) {
      localStorage.setItem('hf_local_model_url', modelUrl.trim());
      setIsModelUrlSaved(true);
      setTimeout(() => setIsModelUrlSaved(false), 2000);
    }
  };

  const handleModelUrlClear = () => {
    localStorage.removeItem('hf_local_model_url');
    setModelUrl('');
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

        <div className="bg-slate-900 rounded-2xl p-6 border border-slate-800 space-y-6">
          <div className="flex items-center gap-3 text-white mb-4">
            <div className="p-2 bg-slate-800 rounded-lg text-green-500">
              <Cpu size={24} />
            </div>
            <h3 className="text-xl font-bold">Local Intelligence</h3>
          </div>

          <p className="text-sm text-slate-400">
            Download optimized models for offline inference. These run entirely on your device's NPU/GPU.
          </p>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Model Download URL</label>
            <input
              type="text"
              value={modelUrl}
              onChange={(e) => setModelUrl(e.target.value)}
              placeholder="https://.../model.bin or .litertlm"
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-white focus:ring-2 focus:ring-green-600 outline-none transition-all placeholder-slate-600 font-mono text-sm"
            />
            <p className="text-xs text-slate-500">
              This pre-fills the Local Protocol download field in chat.
            </p>
            <div className="flex items-center gap-4 pt-1">
              <button
                onClick={handleModelUrlSave}
                className={`flex items-center gap-2 px-5 py-2 rounded-lg font-bold transition-all ${isModelUrlSaved ? 'bg-green-600 text-white' : 'bg-green-700 text-white hover:bg-green-800'}`}
              >
                {isModelUrlSaved ? <ShieldCheck size={16} /> : <Save size={16} />}
                {isModelUrlSaved ? 'Saved' : 'Save URL'}
              </button>

              {modelUrl && (
                <button onClick={handleModelUrlClear} className="text-red-400 text-sm hover:text-red-300 px-3">
                  Clear URL
                </button>
              )}
            </div>
          </div>

          <div className="border border-slate-800 rounded-xl p-4 bg-slate-950/50 flex items-center justify-between">
            <div>
              <h4 className="font-bold text-white">Abliterated Model v1.0</h4>
              <p className="text-xs text-slate-500 font-mono mt-1">550MB • INT8 Quantized</p>
            </div>
            <button className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-cyan-glow rounded-lg transition-colors text-sm font-medium">
              <Download size={16} /> Download
            </button>
          </div>

          <div className="border border-slate-800 rounded-xl p-4 bg-slate-950/50 flex items-center justify-between opacity-50">
            <div>
              <h4 className="font-bold text-slate-400">Llama-3-8B-Optimized</h4>
              <p className="text-xs text-slate-600 font-mono mt-1">4.2GB • Coming Soon</p>
            </div>
            <span className="text-xs text-slate-600 uppercase tracking-widest border border-slate-800 px-2 py-1 rounded">Locked</span>
          </div>
        </div>

        <div className="bg-slate-900 rounded-2xl p-6 border border-slate-800 space-y-6">
          <div className="flex items-center gap-3 text-white mb-4">
            <div className="p-2 bg-slate-800 rounded-lg text-magical">
              <GitBranch size={24} />
            </div>
            <h3 className="text-xl font-bold">Workflow Strategy</h3>
          </div>

          <p className="text-sm text-slate-400">
            Selectively engage different backend models or agentic workflows for specific tasks.
          </p>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Active Inference Protocol</label>
            <select className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-white outline-none focus:border-magical transition-all appearance-none cursor-pointer">
              <option>Cloud Hybrid (Recommended)</option>
              <option>Local-Only (Privacy Focused)</option>
              <option>Chain-of-Thought (Complex)</option>
              <option>Creative Synthesis</option>
            </select>
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
