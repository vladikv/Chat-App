const BASE_URL = window.location.origin;
const MIN_PASSWORD_LENGTH = 6; // keep in sync with the @Size rule in RegisterDTO

let cooldownTimer = null;
let cooling = false;

function switchTab(tab) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.getElementById('login-form').style.display = tab === 'login' ? 'flex' : 'none';
    document.getElementById('register-form').style.display = tab === 'register' ? 'flex' : 'none';
    event.target.classList.add('active');
    document.getElementById('error').textContent = '';
}

// Disable both submit buttons while a request is running (prevents double clicks,
// which would also burn the rate limit)
function setBusy(busy) {
    document.querySelectorAll('#login-form button, #register-form button')
        .forEach(b => { b.disabled = busy; });
}

// After HTTP 429: block the buttons and show a live countdown
function startCooldown(seconds) {
    clearInterval(cooldownTimer);
    cooling = true;
    setBusy(true);
    let left = seconds;

    const tick = () => {
        if (left <= 0) {
            clearInterval(cooldownTimer);
            cooling = false;
            setBusy(false);
            showError('');
            return;
        }
        showError(`Too many attempts. Please try again in ${left} s.`);
        left--;
    };
    tick();
    cooldownTimer = setInterval(tick, 1000);
}

// Turn a failed response into something we can show to the user
async function readError(res, fallback) {
    if (res.status === 429) {
        let seconds = parseInt(res.headers.get('Retry-After'), 10);
        if (!seconds) {
            try { seconds = (await res.clone().json()).retryAfterSeconds; } catch (e) { /* not JSON */ }
        }
        return { rateLimited: true, seconds: seconds || 60 };
    }

    if (res.status >= 500) {
        return { message: 'Server error, try again' };
    }

    try {
        const body = await res.json();
        if (body && body.fields && typeof body.fields === 'object') {
            const first = Object.values(body.fields)[0]; // first field validation message
            if (first) return { message: first };
        }
        if (body && body.message) return { message: body.message };
    } catch (e) { /* not JSON, use the fallback */ }

    return { message: fallback };
}

async function handleFailure(res, fallback) {
    const err = await readError(res, fallback);
    if (err.rateLimited) startCooldown(err.seconds);
    else showError(err.message);
}

async function login() {
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value.trim();

    if (!username || !password) {
        showError('Please fill in all fields');
        return;
    }

    setBusy(true);
    try {
        const res = await fetch(BASE_URL + '/api/account/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) {
            await handleFailure(res, 'Invalid username or password');
            return;
        }

        const data = await res.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('refreshToken', data.refreshToken);
        window.location.href = '/index.html';
    } catch (e) {
        showError('Server error, try again');
    } finally {
        if (!cooling) setBusy(false);
    }
}

async function register() {
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value.trim();

    if (!username || !email || !password) {
        showError('Please fill in all fields');
        return;
    }
    if (!/^\S+@\S+\.\S+$/.test(email)) {
        showError('Please enter a valid email address');
        return;
    }
    if (password.length < MIN_PASSWORD_LENGTH) {
        showError(`Password must be at least ${MIN_PASSWORD_LENGTH} characters`);
        return;
    }

    setBusy(true);
    try {
        const res = await fetch(BASE_URL + '/api/account/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });

        if (!res.ok) {
            await handleFailure(res, 'Registration failed. The username or email may already be taken.');
            return;
        }

        const data = await res.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('refreshToken', data.refreshToken);
        window.location.href = '/index.html';
    } catch (e) {
        showError('Server error, try again');
    } finally {
        if (!cooling) setBusy(false);
    }
}

function showError(msg) {
    document.getElementById('error').textContent = msg;
}

function toggleTheme() {
    const isDark = document.body.classList.toggle('dark');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    document.getElementById('theme-btn').textContent = isDark ? '☀️' : '🌙';
}

// Apply saved theme on load
const savedTheme = localStorage.getItem('theme');
if (savedTheme === 'dark') {
    document.body.classList.add('dark');
    document.getElementById('theme-btn').textContent = '☀️';
}