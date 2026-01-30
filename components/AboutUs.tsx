import React from 'react';
import { ArrowLeft, Globe, Zap, Users } from 'lucide-react';

interface AboutUsProps {
    onBack: () => void;
}

export function AboutUs({ onBack }: AboutUsProps) {
    return (
        <div className="fixed inset-0 bg-deep-void text-white z-[80] overflow-y-auto animate-fade-in">
            <div className="p-6">
                <button
                    onClick={onBack}
                    className="flex items-center gap-2 text-sm text-slate-400 hover:text-white mb-8"
                >
                    <ArrowLeft className="w-4 h-4" /> Back
                </button>

                <div className="space-y-8 max-w-2xl mx-auto">
                    <div className="space-y-2">
                        <h1 className="text-4xl font-serif font-bold text-golden">HawkFranklin</h1>
                        <p className="text-xl text-slate-500 font-light">Research & Innovation Lab</p>
                    </div>

                    <p className="text-slate-300 leading-relaxed text-lg font-light">
                        We are a decentralized collective of researchers, engineers, and designers pushing the boundaries of human-AI interaction. Our mission is to democratize access to unfiltered, raw intelligence.
                    </p>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="p-6 rounded-2xl bg-white/5 border border-white/10 space-y-3">
                            <Zap className="w-6 h-6 text-magical" />
                            <h3 className="font-bold text-lg">Bleeding Edge</h3>
                            <p className="text-sm text-slate-400">Deploying the latest open weights locally on consumer hardware.</p>
                        </div>
                        <div className="p-6 rounded-2xl bg-white/5 border border-white/10 space-y-3">
                            <Globe className="w-6 h-6 text-cyan-glow" />
                            <h3 className="font-bold text-lg">Decentralized</h3>
                            <p className="text-sm text-slate-400">No central servers. local-first architecture for maximum privacy.</p>
                        </div>
                        <div className="p-6 rounded-2xl bg-white/5 border border-white/10 space-y-3">
                            <Users className="w-6 h-6 text-golden" />
                            <h3 className="font-bold text-lg">Open Source</h3>
                            <p className="text-sm text-slate-400">Committed to transparency and community-driven development.</p>
                        </div>
                    </div>

                    <div className="pt-12 border-t border-white/10 text-center">
                        <p className="text-xs text-slate-500 font-mono">
                            EST. 2024 • SEATTLE / DUBAI / TOKYO
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
