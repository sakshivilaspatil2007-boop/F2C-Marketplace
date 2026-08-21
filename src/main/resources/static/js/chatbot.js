// Smart AI Chatbot Assistant dynamic widget injection, speech, image, and suggested questions handlers
document.addEventListener("DOMContentLoaded", () => {
    injectChatbotWidget();
    bindChatbotEvents();
    initSpeechControls();
});

let speakEnabled = false;
let recognition = null;

function injectChatbotWidget() {
    // Add additional chatbot styles
    const style = document.createElement("style");
    style.innerHTML = `
        .chat-window {
            width: 380px;
            height: 520px;
            border-radius: 16px;
            overflow: hidden;
            box-shadow: 0 8px 30px rgba(0,0,0,0.15);
            display: none;
            flex-direction: column;
            position: fixed;
            bottom: 90px;
            right: 20px;
            z-index: 1000;
            background: var(--card-bg, #ffffff);
            border: 1px solid var(--border-color, #e0e0e0);
            transition: all 0.3s ease;
        }
        .chat-header {
            background: linear-gradient(135deg, #2e7d32, #4caf50);
            color: #ffffff;
            padding: 12px 16px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .chat-messages {
            flex: 1;
            padding: 16px;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
            gap: 12px;
            background: var(--bg-color, #f9f9f9);
        }
        .chat-bubble {
            max-width: 85%;
            padding: 10px 14px;
            border-radius: 14px;
            font-size: 13.5px;
            line-height: 1.5;
            word-wrap: break-word;
        }
        .chat-bubble.bot {
            background-color: var(--card-bg, #ffffff);
            color: var(--text-color, #333333);
            border: 1px solid var(--border-color, #e0e0e0);
            align-self: flex-start;
            border-bottom-left-radius: 2px;
        }
        .chat-bubble.user {
            background-color: #2e7d32;
            color: #ffffff;
            align-self: flex-end;
            border-bottom-right-radius: 2px;
        }
        .chat-input-area {
            padding: 10px 16px;
            background: var(--card-bg, #ffffff);
            border-top: 1px solid var(--border-color, #e0e0e0);
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .chat-input {
            flex: 1;
            border: 1px solid var(--border-color, #cccccc);
            border-radius: 20px;
            padding: 6px 14px;
            outline: none;
            background: var(--bg-color, #ffffff);
            color: var(--text-color, #333333);
            font-size: 13px;
        }
        .suggested-questions-grid {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
            margin-top: 8px;
            padding: 0 4px;
        }
        .suggest-btn {
            background: #e8f5e9;
            color: #2e7d32;
            border: 1px solid #c8e6c9;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 11px;
            cursor: pointer;
            transition: all 0.2s ease;
            font-weight: 500;
        }
        .suggest-btn:hover {
            background: #2e7d32;
            color: #ffffff;
        }
        .chat-btn-icon {
            font-size: 18px;
            cursor: pointer;
            color: #666666;
            transition: color 0.2s;
        }
        .chat-btn-icon:hover {
            color: #2e7d32;
        }
        .mic-active {
            color: #dc3545 !important;
            animation: pulse-red 1.5s infinite;
        }
        @keyframes pulse-red {
            0% { transform: scale(1); }
            50% { transform: scale(1.15); }
            100% { transform: scale(1); }
        }
    `;
    document.head.appendChild(style);

    const widgetDiv = document.createElement("div");
    widgetDiv.className = "chatbot-widget";
    widgetDiv.id = "chatbot-assistant-widget";
    widgetDiv.innerHTML = `
        <button class="chat-btn" id="chatbot-toggle-btn" style="position: fixed; bottom: 20px; right: 20px; z-index: 1000; width: 56px; height: 56px; border-radius: 50%; background: #2e7d32; border: none; color: white; box-shadow: 0 4px 15px rgba(0,0,0,0.2);">
            <i class="bi bi-chat-dots-fill fs-4"></i>
        </button>
        <div class="chat-window" id="chatbot-window">
            <div class="chat-header">
                <div class="d-flex align-items-center gap-2">
                    <i class="bi bi-robot fs-4"></i>
                    <div>
                        <h6 class="mb-0 fw-bold">F2C Smart Assistant</h6>
                        <small class="text-white-50" style="font-size: 10px;">AI active & online</small>
                    </div>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <button class="btn btn-link text-white p-0" id="chatbot-volume-btn" title="Toggle Voice Output">
                        <i class="bi bi-volume-mute-fill fs-5" id="volume-icon"></i>
                    </button>
                    <button class="btn-close btn-close-white" id="chatbot-close-btn" aria-label="Close"></button>
                </div>
            </div>
            <div class="chat-messages d-flex flex-column" id="chatbot-messages">
                <div class="chat-bubble bot">
                    Hello! I am your F2C Smart AI Assistant. 🌾<br><br>
                    How can I help you today? Ask me about recipes, seasonal crop info, nutrition, or track your orders!
                    <div class="suggested-questions-grid">
                        <button class="suggest-btn" onclick="sendSuggestion('🥗 Healthy Recipes')">🥗 Healthy Recipes</button>
                        <button class="suggest-btn" onclick="sendSuggestion('📅 Seasonal Vegetables')">📅 Seasonal Vegetables</button>
                        <button class="suggest-btn" onclick="sendSuggestion('🍎 Healthy Fruits')">🍎 Healthy Fruits</button>
                        <button class="suggest-btn" onclick="sendSuggestion('🌾 Organic Farming')">🌾 Organic Farming</button>
                        <button class="suggest-btn" onclick="sendSuggestion('🚚 Track Order')">🚚 Track Order</button>
                    </div>
                </div>
            </div>
            <form class="chat-input-area" id="chatbot-form">
                <i class="bi bi-camera-fill chat-btn-icon" id="chatbot-cam-btn" title="Analyze Crop Image"></i>
                <input type="file" id="chatbot-file-input" accept="image/*" style="display:none;">
                <input type="text" class="chat-input" id="chatbot-input" placeholder="Type or ask..." required autocomplete="off">
                <i class="bi bi-mic-fill chat-btn-icon" id="chatbot-mic-btn" title="Speak message"></i>
                <button type="submit" class="btn btn-success rounded-circle p-2 d-flex align-items-center justify-content-center" style="width: 36px; height: 36px; border: none;">
                    <i class="bi bi-send-fill text-white fs-6"></i>
                </button>
            </form>
        </div>
    `;
    document.body.appendChild(widgetDiv);
}

function bindChatbotEvents() {
    const toggleBtn = document.getElementById("chatbot-toggle-btn");
    const closeBtn = document.getElementById("chatbot-close-btn");
    const chatWindow = document.getElementById("chatbot-window");
    const chatForm = document.getElementById("chatbot-form");
    const chatInput = document.getElementById("chatbot-input");
    const camBtn = document.getElementById("chatbot-cam-btn");
    const fileInput = document.getElementById("chatbot-file-input");

    if (!toggleBtn || !chatWindow) return;

    toggleBtn.addEventListener("click", () => {
        const isVisible = chatWindow.style.display === "flex";
        chatWindow.style.display = isVisible ? "none" : "flex";
        if (!isVisible) {
            chatInput.focus();
            scrollToBottom();
        }
    });

    closeBtn.addEventListener("click", () => {
        chatWindow.style.display = "none";
    });

    chatForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const text = chatInput.value.trim();
        if (!text) return;
        submitChatQuery(text);
    });

    camBtn.addEventListener("click", () => {
        fileInput.click();
    });

    fileInput.addEventListener("change", async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        appendMessage(`Uploading image: ${file.name}...`, "user");
        
        // Show thinking indicator
        const messagesContainer = document.getElementById("chatbot-messages");
        const typingBubble = document.createElement("div");
        typingBubble.className = "chat-bubble bot typing-indicator mb-2";
        typingBubble.innerHTML = '<span class="spinner-grow spinner-grow-sm text-success" role="status"></span> analyzing image...';
        messagesContainer.appendChild(typingBubble);
        scrollToBottom();

        const formData = new FormData();
        formData.append("file", file);

        try {
            const data = await fetch("/api/ai/upload-image", {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem("f2c_token") || ""}`
                },
                body: formData
            }).then(res => res.json());

            typingBubble.remove();
            appendMessage(data.reply, "bot");
            speakText(data.reply);
        } catch (err) {
            typingBubble.remove();
            appendMessage("Failed to process image. Make sure the server is running and database is active.", "bot");
        }
    });
}

async function submitChatQuery(text) {
    const chatInput = document.getElementById("chatbot-input");
    const messagesContainer = document.getElementById("chatbot-messages");

    chatInput.value = "";
    appendMessage(text, "user");

    // Show Typing Indicator
    const typingBubble = document.createElement("div");
    typingBubble.className = "chat-bubble bot typing-indicator mb-2";
    typingBubble.innerHTML = '<span class="spinner-grow spinner-grow-sm text-success" role="status"></span> thinking...';
    messagesContainer.appendChild(typingBubble);
    scrollToBottom();

    try {
        const response = await fetch(`/api/ai/chat?message=${encodeURIComponent(text)}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${localStorage.getItem("f2c_token") || ""}`
            }
        });

        const data = await response.json();
        typingBubble.remove();
        appendMessage(data.reply, "bot");
        speakText(data.reply);
    } catch (err) {
        typingBubble.remove();
        appendMessage("Sorry, I encountered an issue connecting. Please make sure the backend is active.", "bot");
    }
}

function sendSuggestion(val) {
    submitChatQuery(val);
}

function appendMessage(text, sender) {
    const container = document.getElementById("chatbot-messages");
    if (!container) return;

    const bubble = document.createElement("div");
    bubble.className = `chat-bubble ${sender}`;
    
    // Parse formatting (newlines, HTML blocks)
    let formattedText = text;
    if (sender === "bot") {
        // Keep raw HTML intact (like tables, product cards, buttons)
        formattedText = text
            .replace(/\n/g, "<br>")
            .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
            .replace(/\*(.*?)\*/g, "<em>$1</em>");
    } else {
        formattedText = text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
        
    bubble.innerHTML = formattedText;
    container.appendChild(bubble);
    scrollToBottom();
}

function scrollToBottom() {
    const container = document.getElementById("chatbot-messages");
    if (container) {
        container.scrollTop = container.scrollHeight;
    }
}

// --- VOICE & SPEECH CONTROLS (Feature 11) ---
function initSpeechControls() {
    const volBtn = document.getElementById("chatbot-volume-btn");
    const micBtn = document.getElementById("chatbot-mic-btn");
    const volIcon = document.getElementById("volume-icon");
    const chatInput = document.getElementById("chatbot-input");

    // Volume output toggle
    if (volBtn) {
        volBtn.addEventListener("click", () => {
            speakEnabled = !speakEnabled;
            if (speakEnabled) {
                volIcon.className = "bi bi-volume-up-fill fs-5";
                speakText("Voice response enabled.");
            } else {
                volIcon.className = "bi bi-volume-mute-fill fs-5";
                if ('speechSynthesis' in window) {
                    window.speechSynthesis.cancel();
                }
            }
        });
    }

    // Microphone speech recognition
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        recognition = new SpeechRecognition();
        recognition.continuous = false;
        recognition.interimResults = false;
        recognition.lang = 'en-US';

        recognition.onstart = () => {
            micBtn.classList.add("mic-active");
            chatInput.placeholder = "Listening...";
        };

        recognition.onend = () => {
            micBtn.classList.remove("mic-active");
            chatInput.placeholder = "Type or ask...";
        };

        recognition.onresult = (event) => {
            const transcript = event.results[0][0].transcript;
            chatInput.value = transcript;
            submitChatQuery(transcript);
        };

        recognition.onerror = () => {
            micBtn.classList.remove("mic-active");
        };

        micBtn.addEventListener("click", () => {
            try {
                recognition.start();
            } catch (e) {
                recognition.stop();
            }
        });
    } else {
        if (micBtn) micBtn.style.display = "none";
    }
}

function speakText(text) {
    if (!speakEnabled || !('speechSynthesis' in window)) return;
    
    // Strip HTML tags for clean speech
    const cleanText = text.replace(/<[^>]*>/g, '').trim();
    if (!cleanText) return;

    const utterance = new SpeechSynthesisUtterance(cleanText);
    
    // Auto-detect language for correct pronunciation locales
    if (/[अ-ज्ञ]/.test(text)) {
        utterance.lang = 'hi-IN';
    } else {
        utterance.lang = 'en-US';
    }

    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
}

async function addMultipleToCart(items) {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = "login.html";
        return;
    }
    if (user.role !== "CUSTOMER") {
        alert("Only customers can purchase products!");
        return;
    }

    try {
        for (const item of items) {
            await fetch(`/api/cart/add?productId=${item.id}&quantity=${item.qty}`, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem("f2c_token") || ""}`
                }
            });
        }
        alert("All estimated items successfully added to your shopping cart!");
        if (typeof fetchCartCount === "function") fetchCartCount();
    } catch (e) {
        alert("Failed to add some items to your cart.");
    }
}