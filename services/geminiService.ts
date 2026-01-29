import { GoogleGenAI, Type } from "@google/genai";

// Helper to get client with current key
const getAiClient = () => {
    // In a real scenario with strict key selection, we might check window.aistudio
    // but strictly adhering to instructions, we use process.env.API_KEY mostly,
    // and recreate for Veo if needed.
    return new GoogleGenAI({ apiKey: process.env.API_KEY });
};

export const generateTextResponse = async (
  history: { role: string; parts: { text?: string; inlineData?: any }[] }[],
  newMessage: string,
  image?: string,
  useThinking: boolean = false,
  useSearch: boolean = false
) => {
  const ai = getAiClient();
  
  // Select Model
  let modelName = 'gemini-3-flash-preview'; // Default fast
  if (useThinking) modelName = 'gemini-3-pro-preview';
  else if (useSearch) modelName = 'gemini-3-flash-preview';
  else if (image) modelName = 'gemini-3-pro-preview'; // Multimodal

  const config: any = {
    systemInstruction: "You are Aura, a sophisticated personal research assistant developed by HawkFranklin Research. You are helpful, concise, and professional. You can analyze data, generate creative content, and assist with complex tasks.",
  };

  if (useThinking) {
    config.thinkingConfig = { thinkingBudget: 32768 };
  }
  
  if (useSearch) {
    config.tools = [{ googleSearch: {} }];
  }

  // Construct contents
  const contents = history.map(h => ({
    role: h.role,
    parts: h.parts
  }));

  const userParts: any[] = [{ text: newMessage }];
  if (image) {
    userParts.unshift({
      inlineData: {
        mimeType: 'image/jpeg',
        data: image
      }
    });
  }

  contents.push({ role: 'user', parts: userParts });

  try {
    const response = await ai.models.generateContent({
      model: modelName,
      contents: contents,
      config: config
    });

    const groundingChunks = response.candidates?.[0]?.groundingMetadata?.groundingChunks;
    let groundingUrls: Array<{title: string, uri: string}> = [];
    
    if (groundingChunks) {
        groundingChunks.forEach((chunk: any) => {
            if (chunk.web?.uri) {
                groundingUrls.push({ title: chunk.web.title || 'Source', uri: chunk.web.uri });
            }
        });
    }

    return {
      text: response.text || "No text response generated.",
      groundingUrls
    };
  } catch (error) {
    console.error("GenAI Error:", error);
    throw error;
  }
};

export const generateImage = async (prompt: string, isHighQuality: boolean = false) => {
  const ai = getAiClient();
  
  // Check for key selection if high quality (pro model)
  if (isHighQuality && window.aistudio && window.aistudio.hasSelectedApiKey) {
     const hasKey = await window.aistudio.hasSelectedApiKey();
     if (!hasKey) {
        await window.aistudio.openSelectKey();
     }
  }

  // Always re-instantiate if using window selection flow, but standard pattern is usually environment.
  // We will try standard first.
  const model = isHighQuality ? 'gemini-3-pro-image-preview' : 'gemini-2.5-flash-image';
  
  try {
      const response = await ai.models.generateContent({
        model: model,
        contents: {
          parts: [{ text: prompt }]
        },
        config: {
          imageConfig: {
             aspectRatio: "1:1",
             imageSize: isHighQuality ? "2K" : "1K" // 2K only on pro
          }
        }
      });
      
      for (const part of response.candidates?.[0]?.content?.parts || []) {
        if (part.inlineData) {
          return `data:image/png;base64,${part.inlineData.data}`;
        }
      }
      throw new Error("No image generated");
  } catch (e) {
      console.error(e);
      throw e;
  }
};

export const generateVideo = async (prompt: string, aspectRatio: '16:9' | '9:16' = '16:9') => {
    // Veo requires paid key selection
    if (window.aistudio) {
        const hasKey = await window.aistudio.hasSelectedApiKey();
        if (!hasKey) {
            await window.aistudio.openSelectKey();
        }
    }

    // Must recreate client to pick up selected key if any
    const ai = new GoogleGenAI({ apiKey: process.env.API_KEY }); 

    try {
        let operation = await ai.models.generateVideos({
            model: 'veo-3.1-fast-generate-preview',
            prompt: prompt,
            config: {
                numberOfVideos: 1,
                resolution: '720p',
                aspectRatio: aspectRatio
            }
        });

        // Polling
        while (!operation.done) {
            await new Promise(resolve => setTimeout(resolve, 5000));
            operation = await ai.operations.getVideosOperation({ operation: operation });
        }

        const videoUri = operation.response?.generatedVideos?.[0]?.video?.uri;
        if (!videoUri) throw new Error("Video generation failed to return URI");

        // Fetch the actual bytes
        const res = await fetch(`${videoUri}&key=${process.env.API_KEY}`);
        const blob = await res.blob();
        return URL.createObjectURL(blob);
    } catch (e) {
        console.error("Video Gen Error", e);
        throw e;
    }
};

export const generateSpeech = async (text: string) => {
    const ai = getAiClient();
    try {
        const response = await ai.models.generateContent({
            model: "gemini-2.5-flash-preview-tts",
            contents: [{ parts: [{ text: text }] }],
            config: {
                responseModalities: ['AUDIO'],
                speechConfig: {
                    voiceConfig: {
                        prebuiltVoiceConfig: { voiceName: 'Fenrir' }, // Deep, professional voice
                    },
                },
            },
        });
        
        const base64 = response.candidates?.[0]?.content?.parts?.[0]?.inlineData?.data;
        if (!base64) throw new Error("No audio generated");
        return base64;
    } catch (e) {
        console.error("TTS Error", e);
        throw e;
    }
};