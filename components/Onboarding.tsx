import React, { useState } from 'react';
import { ArrowRight, BrainCircuit, ShieldAlert, Sparkles, Lock } from 'lucide-react';

interface OnboardingProps {
    onComplete: () => void;
}

export function Onboarding({ onComplete }: OnboardingProps) {
    const [step, setStep] = useState(0);

    const slides = [
        {
            icon: BrainCircuit,
            title: "Unfiltered Intelligence",
            desc: "Connect directly to the raw model. No guardrails. No filters. Pure cognitive processing.",
            color: "text-cyan-glow"
        },
        {
            icon: ShieldAlert,
            title: "Absolute Privacy",
            desc: "Your data stays local. No cloud logging. We don't see what you see.",
            color: "text-magical"
        },
        {
            icon: Lock,
            title: "System Protocol",
            desc: "Aura requires access to your visual and auditory sensors (Camera & Mic) to perceive reality.",
            color: "text-red-500",
            action: "Grant Permissions"
        },
        {
            icon: Sparkles,
            title: "Mischief Managed",
            desc: "You are now part of the Aura protocol. Use your power wisely.",
            color: "text-golden"
        }
    ];

    const handleNext = async () => {
        // Permission Step is index 2
        if (step === 2) {
            try {
                // Trigger OS prompts
                await navigator.mediaDevices.getUserMedia({ audio: true, video: true });
                // Proceed if successful
                setStep(step + 1);
            } catch (err) {
                console.warn("Permissions denied", err);
                alert("Access denied. Aura will be limited to text only.");
                setStep(step + 1);
            }
            return;
        }

        if (step < slides.length - 1) {
            setStep(step + 1);
        } else {
            onComplete();
        }
    };

    const CurrentIcon = slides[step].icon;

    return (
        <div className="fixed inset-0 bg-deep-void text-white z-[90] flex flex-col">
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center space-y-8">
                <div className={`w-32 h-32 rounded-full bg-white/5 border border-white/10 flex items-center justify-center animate-float shadow-[0_0_50px_rgba(0,0,0,0.5)]`}>
                    <CurrentIcon className={`w-12 h-12 ${slides[step].color}`} />
                </div>

                <div className="space-y-4 max-w-xs mx-auto animate-fade-in" key={step}>
                    <h2 className="text-2xl font-bold font-serif">{slides[step].title}</h2>
                    <p className="text-sm text-slate-400 leading-relaxed font-light">
                        {slides[step].desc}
                    </p>
                </div>
            </div>

            <div className="p-8 pb-12 flex items-center justify-between">
                <div className="flex gap-2">
                    {slides.map((_, i) => (
                        <div
                            key={i}
                            className={`h-1.5 rounded-full transition-all duration-300 ${i === step ? 'w-8 bg-white' : 'w-2 bg-white/20'}`}
                        />
                    ))}
                </div>

                <button
                    onClick={handleNext}
                    className={`w-14 h-14 rounded-full flex items-center justify-center hover:scale-105 active:scale-95 transition-transform ${slides[step].action ? 'bg-red-500 text-white w-auto px-6 gap-2' : 'bg-white text-black'}`}
                >
                    {slides[step].action ? (
                        <>
                            <span className="text-xs font-bold uppercase tracking-wider">{slides[step].action}</span>
                            <ArrowRight className="w-4 h-4" />
                        </>
                    ) : (
                        <ArrowRight className="w-5 h-5" />
                    )}
                </button>
            </div>
        </div>
    );
}
