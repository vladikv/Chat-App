const TOKEN = localStorage.getItem('token');
const BASE_URL = window.location.origin;
const ME = localStorage.getItem('username');

if (!TOKEN) window.location.href = '/login/login.html';

document.getElementById('user-info').textContent = '👤 ' + ME;

let currentRoomId = null;
let currentSubscription = null;
let currentTypingSubscription = null;

const socket = new SockJS(BASE_URL + '/ws');
const stompClient = Stomp.over(socket);
stompClient.debug = null;

stompClient.connect({ Authorization: 'Bearer ' + TOKEN }, function () {
    document.getElementById('status').textContent = '🟢 Connected';
    loadRooms();

    stompClient.subscribe('/topic/online-users', function (msg) {
        const users = JSON.parse(msg.body);
        updateOnlineUsers(users);
    });

    setTimeout(() => {
        fetch(BASE_URL + '/api/online-users', {
            headers: { Authorization: 'Bearer ' + TOKEN }
        })
            .then(r => r.json())
            .then(users => updateOnlineUsers(users));
    }, 500);
});

function logout() {
    localStorage.clear();
    window.location.href = '/login/login.html';
}

function toggleNewRoom() {
    const el = document.getElementById('new-room');
    el.style.display = el.style.display === 'none' ? 'flex' : 'none';
    if (el.style.display === 'flex') document.getElementById('room-input').focus();
}

async function loadRooms() {
    const res = await fetch(BASE_URL + '/api/rooms', {
        headers: { Authorization: 'Bearer ' + TOKEN }
    });
    const rooms = await res.json();
    const list = document.getElementById('rooms-list');
    list.innerHTML = '';
    rooms.forEach(room => {
        const div = document.createElement('div');
        div.className = 'room-item';
        div.textContent = room.name;
        div.onclick = () => openRoom(room.id, room.name, div);
        list.appendChild(div);
    });
}

async function createRoom() {
    const input = document.getElementById('room-input');
    const name = input.value.trim();
    if (!name) return;

    await fetch(BASE_URL + '/api/rooms', {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + TOKEN, 'Content-Type': 'application/json' },
        body: JSON.stringify({ name })
    });

    input.value = '';
    toggleNewRoom();
    loadRooms();
}

function openRoom(roomId, roomName, el) {
    if (window.innerWidth <= 600) {
        document.getElementById('sidebar').classList.add('hidden');
    }
    document.querySelectorAll('.room-item').forEach(r => r.classList.remove('active'));
    el.classList.add('active');

    document.getElementById('room-title').textContent = '#' + roomName;
    document.getElementById('bottom').style.display = 'flex';
    document.getElementById('chat').innerHTML = '';
    document.getElementById('typing-indicator').innerHTML = '';

    if (currentSubscription) currentSubscription.unsubscribe();
    if (currentTypingSubscription) currentTypingSubscription.unsubscribe();

    currentRoomId = roomId;

    fetch(BASE_URL + `/api/rooms/${roomId}/messages`, {
        headers: { Authorization: 'Bearer ' + TOKEN }
    })
        .then(r => r.json())
        .then(messages => messages.forEach(addMessage));

    currentSubscription = stompClient.subscribe(`/topic/room.${roomId}`, function (msg) {
        addMessage(JSON.parse(msg.body));
    });

    currentTypingSubscription = stompClient.subscribe(`/topic/typing.${roomId}`, function (msg) {
        showTyping(msg.body.replace(/"/g, ''));
    });
}

function send() {
    const input = document.getElementById('input');
    const text = input.value.trim();
    if (!text || !currentRoomId) return;
    stompClient.send(`/app/chat.send/${currentRoomId}`, {}, JSON.stringify({ content: text }));
    input.value = '';
}

function getUserColor(username) {
    const colors = ['#007aff', '#34c759', '#ff9500', '#af52de', '#ff2d55', '#5ac8fa', '#ff6b35'];
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
}

function addMessage(msg) {
    const chat = document.getElementById('chat');
    const isMe = msg.senderUsername === ME;
    const color = getUserColor(msg.senderUsername);
    const div = document.createElement('div');
    div.className = 'message ' + (isMe ? 'mine' : 'other');
    div.innerHTML = `
        <div class="sender" style="color: ${isMe ? '#8e8e93' : color}">${msg.senderUsername}</div>
        <div class="bubble">${msg.content}</div>
        <div class="time">${new Date(msg.sentAt).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</div>
    `;
    chat.appendChild(div);
    chat.scrollTop = chat.scrollHeight;
}

function updateOnlineUsers(users) {
    const others = users.filter(u => u !== ME);
    const el = document.getElementById('user-info');
    el.innerHTML = `
        <div class="online-label">ONLINE — ${others.length}</div>
        ${others.length === 0 ? '<div style="font-size:13px;color:#8e8e93;padding:2px 0">No one else online</div>' : ''}
        ${others.map(u => `
            <div class="online-user">
                <span class="online-dot"></span>${u}
            </div>
        `).join('')}
    `;
}

let typingTimeout = null;

function showTyping(username) {
    if (username === ME) return;
    const el = document.getElementById('typing-indicator');
    el.innerHTML = `
        <div class="typing-dots">
            <span></span><span></span><span></span>
        </div>
        ${username} is typing...
    `;
    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
        el.innerHTML = '';
    }, 2000);
}

let typingSent = false;

function notifyTyping() {
    if (typingSent || !currentRoomId) return;
    typingSent = true;
    stompClient.send(`/app/typing/${currentRoomId}`, {}, '');
    setTimeout(() => { typingSent = false; }, 1500);
}

function showSidebar() {
    document.getElementById('sidebar').classList.remove('hidden');
}