import React, { useState, useEffect } from 'react';
import { Fingerprint, Lock, Wand2 } from 'lucide-react';

interface ConsentGateProps {
    onConsent: () => void;
}

export function ConsentGate({ onConsent }: ConsentGateProps) {
    const [isLongPressing, setIsLongPressing] = useState(false);
    const [progress, setProgress] = useState(0);

    useEffect(() => {
        let interval: any;
        if (isLongPressing) {
            interval = setInterval(() => {
                setProgress((prev) => {
                    if (prev >= 100) {
                        clearInterval(interval);
                        onConsent();
                        return 100;
                    }
                    return prev + 2; // Speed of fill
                });
            }, 20);
        } else {
            setProgress(0);
        }
        return () => clearInterval(interval);
    }, [isLongPressing, onConsent]);

    return (
        <div className="fixed inset-0 bg-deep-void text-white z-[100] flex flex-col items-center justify-center p-6 overflow-hidden">
            {/* Dynamic Background */}
            <div className="absolute inset-0 pointer-events-none">
                <div className="absolute top-0 right-0 w-96 h-96 bg-magical/10 blur-[120px] rounded-full animate-float" />
                <div className="absolute bottom-0 left-0 w-96 h-96 bg-cyan-glow/5 blur-[120px] rounded-full animate-float" style={{ animationDelay: '2s' }} />
            </div>

            <div className="relative z-10 max-w-md w-full text-center space-y-12">
                <div className="space-y-4 animate-fade-in">
                    <div className="w-20 h-20 mx-auto rounded-full bg-glass border border-white/10 flex items-center justify-center shadow-[0_0_30px_rgba(139,92,246,0.2)]">
                        <Lock className="w-8 h-8 text-magical" />
                    </div>
                    <h1 className="text-3xl font-serif font-bold text-transparent bg-clip-text bg-gradient-to-r from-magical to-cyan-glow">
                        Restricted Access
                    </h1>
                    <p className="font-mono text-xs text-slate-400 tracking-widest uppercase">
                        Protocol: Unfiltered_XI
                    </p>
                </div>

                <div className="space-y-6 animate-fade-in" style={{ animationDelay: '0.2s' }}>
                    <p className="text-lg text-slate-200 font-serif italic leading-relaxed">
                        "I solemnly swear that I am up to no good."
                    </p>
                    <div className="p-4 bg-white/5 border border-red-500/20 rounded-xl">
                        <p className="text-sm text-red-300 font-light">
                            <strong className="block text-red-500 mb-1">WARNING: UNFILTERED AI</strong>
                            This system provides raw, unmoderated outputs. By entering, you acknowledge the potential for unpredictable, offensive, or dangerous content. Proceed with caution.
                        </p>
                    </div>
                </div>

                <div className="animate-fade-in" style={{ animationDelay: '0.4s' }}>
                    <div
                        className="relative w-24 h-24 mx-auto rounded-full border-2 border-white/10 flex items-center justify-center cursor-pointer select-none transition-all duration-300 active:scale-95 hover:border-magical/50"
                        onMouseDown={() => setIsLongPressing(true)}
                        onMouseUp={() => setIsLongPressing(false)}
                        onMouseLeave={() => setIsLongPressing(false)}
                        onTouchStart={() => setIsLongPressing(true)}
                        onTouchEnd={() => setIsLongPressing(false)}
                    >
                        {/* Progress Circle */}
                        <svg className="absolute inset-0 w-full h-full -rotate-90" viewBox="0 0 100 100">
                            <circle
                                cx="50"
                                cy="50"
                                r="48"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                className="text-magical transition-all duration-100"
                                strokeDasharray="301.59"
                                strokeDashoffset={301.59 - (301.59 * progress) / 100}
                                strokeLinecap="round"
                            />
                        </svg>
                        <Fingerprint className={`w-8 h-8 text-slate-400 transition-colors ${progress > 0 ? 'text-magical' : ''}`} />
                    </div>
                    <p className="mt-4 text-[10px] text-slate-500 uppercase tracking-widest">
                        {progress > 0 ? 'Authenticating...' : 'Hold to Enter'}
                    </p>
                </div>
            </div>

            <div className="absolute bottom-6 text-center animate-fade-in" style={{ animationDelay: '0.6s' }}>
                <p className="text-[10px] text-slate-600 font-mono">HawkFranklin Research &copy; 2026</p>
            </div>
        </div>
    );
}
