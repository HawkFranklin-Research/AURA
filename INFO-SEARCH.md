# Info Search Notes

## Q1: Is there a way to integrate or use Ollama or GGUF models inside an APK? Are people doing it? How are they running things like FunctionGemma?

**Short answer:**
- **GGUF inside APK:** Yes, commonly done via **llama.cpp + JNI** (on-device inference).
- **Ollama inside APK:** Uncommon; most people run Ollama separately (Termux/proot) and connect over `localhost`.

**What people actually do (best → meh):**
1) **Embed GGUF inference directly in APK** (recommended). Use a llama.cpp-based Android wrapper via JNI.
2) **Run Ollama separately** (Termux), app calls local API. Works but not “inside APK.”

**FunctionGemma on-device:**
- FunctionGemma is distributed in **GGUF** on Hugging Face. It’s small (~270M) and used for tool calling.
- You run it like any GGUF model (llama.cpp), then implement **tool execution** in your app: prompt → model emits JSON tool call → you execute → feed result back.

**Minimal working code — GGUF in APK + tool calling (Kotlin):**

A) Run a GGUF model (llmedge example)
```kotlin
import io.aatricks.llmedge.LLMEdgeManager
import kotlinx.coroutines.*

fun runLocalLLM(context: android.content.Context) {
  CoroutineScope(Dispatchers.IO).launch {
    val reply = LLMEdgeManager.generateText(
      context = context,
      params = LLMEdgeManager.TextGenerationParams(
        prompt = "Say hi in 5 words."
      )
    )

    withContext(Dispatchers.Main) {
      println(reply)
    }
  }
}
```

Download + load GGUF from Hugging Face (same library)
```kotlin
import io.aatricks.llmedge.SmolLM

suspend fun loadGgufFromHF(context: android.content.Context): java.io.File {
  val smol = SmolLM()
  val dl = smol.loadFromHuggingFace(
    context = context,
    modelId = "unsloth/functiongemma-270m-it-GGUF",
    filename = "functiongemma-270m-it-Q4_K_M.gguf",
    forceDownload = false,
    preferSystemDownloader = true
  )
  return dl.file
}
```

B) Tool-calling loop (FunctionGemma-style)
```kotlin
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable data class ToolCall(val tool: String, val arguments: JsonObject)

val json = Json { ignoreUnknownKeys = true }

suspend fun toolCallingTurn(context: android.content.Context, user: String): String {
  val system = """
You are a function-calling model.
If you need a tool, output ONLY valid JSON:
{"tool":"<name>","arguments":{...}}
Otherwise output normal text.
Available tools: get_time
""".trimIndent()

  val first = LLMEdgeManager.generateText(
    context,
    LLMEdgeManager.TextGenerationParams(prompt = "$system\nUser: $user\nAssistant:")
  ).trim()

  if (first.startsWith("{")) {
    val call = json.decodeFromString<ToolCall>(first)

    val toolResult: String = when (call.tool) {
      "get_time" -> java.time.ZonedDateTime.now().toString()
      else -> "ERROR: unknown tool ${call.tool}"
    }

    return LLMEdgeManager.generateText(
      context,
      LLMEdgeManager.TextGenerationParams(
        prompt = """
$system
User: $user
Assistant: $first
Tool result: $toolResult
Assistant:
""".trimIndent()
      )
    )
  }

  return first
}
```

**Conclusion:**
- **Inside APK:** GGUF + llama.cpp (JNI) is the standard path.
- **Ollama:** Usually runs outside APK (Termux) and the app calls localhost.
- **FunctionGemma:** Run GGUF with llama.cpp + tool-calling loop.

---

## Q2: Are people doing on-device RAG? Can it be packed with NPM/Capacitor? Any GitHub repos?

**Short answer:**
- **Yes**, local RAG on mobile is actively done.
- **Yes**, you can ship a Capacitor app (NPM UI) with a **native inference plugin** (llama.cpp) + local embeddings + vector DB.

**Approach:**
1) Chunk docs
2) Embed chunks (native ONNX/Edge embeddings)
3) Store vectors (SQLite/HNSW/custom)
4) Retrieve top-k
5) Feed to local GGUF model via plugin

**GitHub repos / references:**
- **Capacitor + llama.cpp:**
  - arusatech/llama-cpp — llama.cpp + CapacitorJS support
  - cantoo-scribe/capacitor-llama — Capacitor binding for llama.cpp

- **On-device RAG
**  - google-ai-edge/ai-edge-apis (examples/rag)
  - google-ai-edge/gallery (AI Edge Gallery)
  - shubham0204/OnDevice-RAG-Android
  - Aatricks/llmedge + llmedge-examples
  - farmaker47/llmedge_gguf
  - nerve-sparks/iris_android

- **Ollama on Android (usually Termux):**
  - SMuflhi/ollama-app-for-Android-

**Minimal Capacitor usage shape (example):**
```ts
import { LlamaCpp } from "llama-cpp-capacitor";

await LlamaCpp.loadModel({ path: "models/mistral-7b-instruct.Q4_K_M.gguf" });

const out = await LlamaCpp.generate({
  prompt: "Answer in 1 sentence: what is RAG?",
  maxTokens: 128,
  temperature: 0.2
});

console.log(out.text);
```

**Conclusion:**
- **On-device RAG:** Yes, already in real apps.
- **Capacitor/NPM:** Yes, if you use a native plugin for inference.
- **Ollama inside APK:** Uncommon; typical setup is Termux + localhost.
