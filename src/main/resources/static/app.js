let TOKEN = localStorage.getItem('token');
const BASE_URL = window.location.origin;
const ME = localStorage.getItem('username');

function getToken() {
    TOKEN = localStorage.getItem('token');
    return TOKEN;
}

if (!TOKEN) window.location.href = '/login/login.html';

const userInfo = document.getElementById('user-info');
if (userInfo) userInfo.textContent = '👤 ' + ME;

let currentRoomId = null;
let currentSubscription = null;
let currentTypingSubscription = null;

let contextMessageId = null;
let contextOriginalContent = null;

const socket = new SockJS(BASE_URL + '/ws');
const stompClient = Stomp.over(socket);
stompClient.debug = null;

stompClient.connect({ Authorization: 'Bearer ' + getToken() }, function () {
    document.getElementById('status').textContent = '🟢 Connected';
    loadRooms();

    // Set avatar and username
    const btn = document.getElementById('profile-btn');
    const color = getUserColor(ME);
    btn.style.cssText = `width:32px;height:32px;border-radius:50%;background:${color};color:white;font-weight:700;font-size:14px;display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0;`;
    btn.textContent = ME.charAt(0).toUpperCase();

    const usernameLabel = document.getElementById('username-label');
    if (usernameLabel) usernameLabel.textContent = ME;

    stompClient.subscribe('/topic/online-users', function (msg) {
        const users = JSON.parse(msg.body);
        updateOnlineUsers(users);
    });

    setTimeout(() => {
        fetch(BASE_URL + '/api/online-users', {
            headers: { Authorization: 'Bearer ' + getToken() }
        })
            .then(r => r.json())
            .then(users => updateOnlineUsers(users));
    }, 500);
});

function logout() {
    localStorage.clear();
    window.location.href = '/login/login.html';
}

function toggleLeftSidebar() {
    document.getElementById('sidebar').classList.toggle('collapsed');
}

function toggleNewRoom() {
    const el = document.getElementById('new-room');
    el.style.display = el.style.display === 'none' ? 'flex' : 'none';
    if (el.style.display === 'flex') document.getElementById('room-input').focus();
}

function toggleRightSidebar() {
    document.getElementById('right-sidebar').classList.toggle('hidden');
}


async function loadRooms() {
    const res = await fetch(BASE_URL + '/api/rooms', {
        headers: { Authorization: 'Bearer ' + getToken() }
    });
    const rooms = await res.json();
    const list = document.getElementById('rooms-list');
    list.innerHTML = '';
    rooms.forEach(room => {
        const div = document.createElement('div');
        div.className = 'room-item';
        div.id = 'room-' + room.id;
        div.innerHTML = `<span class="room-name">${room.name}</span>`;
        div.onclick = () => openRoom(room.id, room.name, div);
        if (room.createdBy === ME) {
            div.ondblclick = (e) => {
                e.stopPropagation();
                startRenameRoom(room.id, div);
            };
        }
        list.appendChild(div);
    });
}

function startRenameRoom(roomId, div) {
    const currentName = div.querySelector('.room-name').textContent;
    div.innerHTML = `
        <input class="room-rename-input" value="${currentName}" 
               onkeydown="handleRenameKey(event, ${roomId}, this)"
               onblur="cancelRename(this, '${currentName}')"/>
    `;
    const input = div.querySelector('input');
    input.focus();
    input.select();
}

function handleRenameKey(event, roomId, input) {
    if (event.key === 'Enter') {
        saveRename(roomId, input);
    } else if (event.key === 'Escape') {
        cancelRename(input, input.defaultValue);
    }
}

async function saveRename(roomId, input) {
    const newName = input.value.trim();
    if (!newName) return;

    const res = await fetch(BASE_URL + `/api/rooms/${roomId}`, {
        method: 'PUT',
        headers: {
            Authorization: 'Bearer ' + getToken(),
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: newName })
    });

    if (res.ok) {
        const room = await res.json();
        loadRooms();
        if (currentRoomId === roomId) {
            document.getElementById('room-title').textContent = '#' + room.name;
        }
    } else {
        alert('Name already taken!');
        loadRooms();
    }
}

function cancelRename(input, originalName) {
    loadRooms();
}

async function createRoom() {
    const input = document.getElementById('room-input');
    const name = input.value.trim();
    if (!name) return;

    await fetch(BASE_URL + '/api/rooms', {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + getToken(), 'Content-Type': 'application/json' },
        body: JSON.stringify({ name })
    });

    input.value = '';
    toggleNewRoom();
    loadRooms();
    loadUsers();
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
        headers: { Authorization: 'Bearer ' + getToken() }
    })
        .then(r => {
            if (!r.ok) {
                console.error('Failed to load messages:', r.status);
                return [];
            }
            return r.json();
        })
        .then(messages => messages && messages.forEach(addMessage))
        .catch(e => console.error('Messages error:', e));

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

    const editingId = input.dataset.editingId;

    if (editingId) {
        // Send edit via WebSocket
        stompClient.send(`/app/chat.edit/${currentRoomId}/${editingId}`, {},
            JSON.stringify({ content: text }));
        cancelEdit();
    } else {
        stompClient.send(`/app/chat.send/${currentRoomId}`, {},
            JSON.stringify({ content: text }));
    }

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

function deleteMessage(messageId) {
    fetch(BASE_URL + `/api/rooms/${currentRoomId}/messages/${messageId}`, {
        method: 'DELETE',
        headers: { Authorization: 'Bearer ' + getToken() }
    }).then(res => {
        if (res.ok) {
            const el = document.getElementById('msg-' + messageId);
            if (el) el.remove();
            // Notify others via WebSocket
            stompClient.send(`/app/chat.delete/${currentRoomId}/${messageId}`, {}, '');
        }
    });
}
function addMessage(msg) {
    if (msg.deletedId) {
        const el = document.getElementById('msg-' + msg.deletedId);
        if (el) el.remove();
        return;
    }

    // Update existing message if edited
    const existing = document.getElementById('msg-' + msg.id);
    if (existing) {
        const bubble = existing.querySelector('.bubble');
        bubble.innerHTML = `
            ${msg.content}
            ${msg.edited ? '<span class="edited-label">edited</span>' : ''}
        `;
        return;
    }

    const chat = document.getElementById('chat');
    const isMe = msg.senderUsername === ME;
    const color = getUserColor(msg.senderUsername);
    const div = document.createElement('div');
    div.className = 'message ' + (isMe ? 'mine' : 'other');
    div.id = 'msg-' + msg.id;
    div.innerHTML = `
        <div class="sender" style="color: ${isMe ? '#8e8e93' : color}">${msg.senderUsername}</div>
        <div class="bubble">
            ${msg.content}
            ${msg.edited ? '<span class="edited-label">edited</span>' : ''}
        </div>
        <div class="time">${new Date(msg.sentAt).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</div>
    `;

    // Context menu
    if (isMe) {
        div.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            showContextMenu(e.clientX, e.clientY, msg.id, msg.content, true);
        });

        // Long press for mobile
        let pressTimer;
        div.addEventListener('touchstart', () => {
            pressTimer = setTimeout(() => {
                const rect = div.getBoundingClientRect();
                showContextMenu(rect.left + rect.width / 2, rect.top, msg.id, msg.content, true);
            }, 500);
        });
        div.addEventListener('touchend', () => clearTimeout(pressTimer));
        div.addEventListener('touchmove', () => clearTimeout(pressTimer));
    }

    chat.appendChild(div);
    chat.scrollTop = chat.scrollHeight;
}

function updateOnlineUsers(users) {
    loadUsers();
}

const OFFLINE_LIMIT = 10;
let showAllOffline = false;

async function loadUsers() {
    const res = await fetch(BASE_URL + '/api/users', {
        headers: { Authorization: 'Bearer ' + getToken() }
    });
    const users = await res.json();

    const online = users.filter(u => u.online && u.username !== ME);
    const offline = users.filter(u => !u.online && u.username !== ME);

    // Online list
    document.getElementById('online-list').innerHTML = online.length === 0
        ? '<div class="member-item" style="color:#8e8e93">No one else online</div>'
        : online.map(u => `
            <div class="member-item online">
                <span class="status-dot"></span>${u.username}
            </div>
        `).join('');

    // Offline list
    const visible = showAllOffline ? offline : offline.slice(0, OFFLINE_LIMIT);
    document.getElementById('offline-list').innerHTML = visible.map(u => `
        <div class="member-item offline">
            <span class="status-dot"></span>${u.username}
        </div>
    `).join('');

    // Show more button
    const more = document.getElementById('offline-more');
    if (offline.length > OFFLINE_LIMIT && !showAllOffline) {
        more.textContent = `+ ${offline.length - OFFLINE_LIMIT} more...`;
        more.onclick = () => { showAllOffline = true; loadUsers(); };
    } else {
        more.textContent = '';
    }
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

function showContextMenu(x, y, messageId, content, isMe) {
    contextMessageId = messageId;
    contextOriginalContent = content;

    const menu = document.getElementById('context-menu');
    document.getElementById('ctx-edit').style.display = isMe ? 'flex' : 'none';
    document.getElementById('ctx-delete').style.display = isMe ? 'flex' : 'none';

    menu.classList.add('visible');

    // Position menu within viewport
    const menuWidth = 160;
    const menuHeight = 100;
    const left = Math.min(x, window.innerWidth - menuWidth - 10);
    const top = Math.min(y, window.innerHeight - menuHeight - 10);

    menu.style.left = left + 'px';
    menu.style.top = top + 'px';
}

function hideContextMenu() {
    document.getElementById('context-menu').classList.remove('visible');
    contextMessageId = null;
}

function contextDelete() {
    if (!contextMessageId) return;
    deleteMessage(contextMessageId);
    hideContextMenu();
}

function contextEdit() {
    if (!contextMessageId) return;

    const input = document.getElementById('input');
    input.value = contextOriginalContent;
    input.focus();
    input.dataset.editingId = contextMessageId;

    // Show editing indicator
    document.getElementById('typing-indicator').innerHTML = `
        <div style="display:flex;align-items:center;gap:8px;color:#007aff;font-size:13px;">
            ✏️ Editing message 
            <span onclick="cancelEdit()" style="cursor:pointer;color:#ff3b30;">✕ Cancel</span>
        </div>
    `;

    hideContextMenu();
}

function cancelEdit() {
    const input = document.getElementById('input');
    input.value = '';
    delete input.dataset.editingId;
    document.getElementById('typing-indicator').innerHTML = '';
}

// Close menu on click outside
document.addEventListener('click', hideContextMenu);
document.addEventListener('touchstart', (e) => {
    if (!e.target.closest('#context-menu')) hideContextMenu();
});

function toggleTheme() {
    const isDark = document.body.classList.toggle('dark');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    document.getElementById('theme-btn').textContent = isDark ? '☀️' : '🌙';
}

// Load saved theme
const savedTheme = localStorage.getItem('theme');
if (savedTheme === 'dark') {
    document.body.classList.add('dark');
    const btn = document.getElementById('theme-btn');
    if (btn) btn.textContent = '☀️';
}