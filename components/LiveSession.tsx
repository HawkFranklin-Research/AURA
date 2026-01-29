import React, { useEffect, useRef, useState } from 'react';
import { Mic, MicOff, X, Radio } from 'lucide-react';
import { GoogleGenAI, LiveServerMessage, Modality } from '@google/genai';
import { createPcmBlob, decodeAudioData, base64ToUint8Array } from '../services/audioUtils';

interface Props {
  onClose: () => void;
}

export const LiveSession: React.FC<Props> = ({ onClose }) => {
  const [isConnected, setIsConnected] = useState(false);
  const [isMicOn, setIsMicOn] = useState(true);
  const [status, setStatus] = useState("Initializing...");
  const [volume, setVolume] = useState(0);

  // Refs for audio handling
  const audioContextRef = useRef<AudioContext | null>(null);
  const inputContextRef = useRef<AudioContext | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const nextStartTimeRef = useRef<number>(0);
  const sessionRef = useRef<any>(null); // To hold the session object
  const mountedRef = useRef(true);

  useEffect(() => {
    return () => {
      mountedRef.current = false;
      cleanup();
    };
  }, []);

  const cleanup = () => {
    if (streamRef.current) {
        streamRef.current.getTracks().forEach(track => track.stop());
    }
    if (audioContextRef.current) audioContextRef.current.close();
    if (inputContextRef.current) inputContextRef.current.close();
  };

  const startSession = async () => {
    try {
      setStatus("Connecting to Aura Neural Network...");
      const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });
      
      // Setup Audio Contexts
      audioContextRef.current = new (window.AudioContext || (window as any).webkitAudioContext)({ sampleRate: 24000 });
      inputContextRef.current = new (window.AudioContext || (window as any).webkitAudioContext)({ sampleRate: 16000 });
      
      // Get Mic
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;

      // Connect to Gemini Live
      const sessionPromise = ai.live.connect({
        model: 'gemini-2.5-flash-native-audio-preview-12-2025',
        callbacks: {
          onopen: () => {
            if (!mountedRef.current) return;
            setIsConnected(true);
            setStatus("Connected. Listening...");
            
            // Setup Input Processing
            if (!inputContextRef.current) return;
            const source = inputContextRef.current.createMediaStreamSource(stream);
            const processor = inputContextRef.current.createScriptProcessor(4096, 1, 1);
            
            processor.onaudioprocess = (e) => {
               if (!isMicOn) return; // Mute logic
               const inputData = e.inputBuffer.getChannelData(0);
               // Visualization logic simple
               let sum = 0;
               for(let i=0; i<inputData.length; i++) sum += inputData[i] * inputData[i];
               setVolume(Math.sqrt(sum / inputData.length) * 100);

               const pcmBlob = createPcmBlob(inputData);
               sessionPromise.then(session => {
                  session.sendRealtimeInput({ media: pcmBlob });
               });
            };
            
            source.connect(processor);
            processor.connect(inputContextRef.current.destination);
          },
          onmessage: async (msg: LiveServerMessage) => {
             if (!mountedRef.current) return;
             
             // Handle Audio Output
             const audioData = msg.serverContent?.modelTurn?.parts?.[0]?.inlineData?.data;
             if (audioData && audioContextRef.current) {
                const ctx = audioContextRef.current;
                nextStartTimeRef.current = Math.max(nextStartTimeRef.current, ctx.currentTime);
                
                const buffer = await decodeAudioData(base64ToUint8Array(audioData), ctx);
                const source = ctx.createBufferSource();
                source.buffer = buffer;
                source.connect(ctx.destination);
                source.start(nextStartTimeRef.current);
                nextStartTimeRef.current += buffer.duration;
             }

             // Handle Interruption
             if (msg.serverContent?.interrupted) {
                nextStartTimeRef.current = 0;
             }
          },
          onclose: () => {
             if(mountedRef.current) setStatus("Connection closed.");
          },
          onerror: (err) => {
             console.error(err);
             if(mountedRef.current) setStatus("Error occurred.");
          }
        },
        config: {
           responseModalities: [Modality.AUDIO],
           speechConfig: {
              voiceConfig: { prebuiltVoiceConfig: { voiceName: 'Zephyr' } }
           },
           systemInstruction: "You are Aura, the user's personal AI voice assistant. Be professional, concise, and helpful."
        }
      });
      
      sessionRef.current = sessionPromise;

    } catch (e) {
      console.error(e);
      setStatus("Failed to initialize audio.");
    }
  };

  useEffect(() => {
     startSession();
     // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="flex flex-col items-center justify-center h-full bg-slate-900 text-white relative p-6">
       <button onClick={onClose} className="absolute top-4 right-4 p-2 bg-slate-800 rounded-full hover:bg-slate-700">
         <X className="text-slate-400" />
       </button>

       {/* Visualizer */}
       <div className="relative w-48 h-48 flex items-center justify-center mb-12">
          {/* Outer Glow */}
          <div className={`absolute inset-0 rounded-full bg-yellow-600 opacity-20 blur-xl transition-all duration-100`} 
               style={{ transform: `scale(${1 + volume/20})` }}></div>
          
          <div className="w-40 h-40 rounded-full bg-slate-800 border-4 border-yellow-600 flex items-center justify-center shadow-[0_0_30px_rgba(202,138,4,0.3)]">
              <Radio size={64} className={`text-yellow-500 ${isConnected ? 'animate-pulse' : ''}`} />
          </div>
       </div>

       <h2 className="text-2xl font-bold text-yellow-500 mb-2 font-serif">Aura Live</h2>
       <p className="text-slate-400 mb-8 font-mono text-sm">{status}</p>

       <div className="flex gap-6">
          <button 
             onClick={() => setIsMicOn(!isMicOn)}
             className={`p-6 rounded-full transition-all ${isMicOn ? 'bg-white text-slate-900' : 'bg-red-500 text-white'}`}
          >
             {isMicOn ? <Mic size={32} /> : <MicOff size={32} />}
          </button>
       </div>
    </div>
  );
};