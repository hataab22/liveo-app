from flask import Flask, request, jsonify
from flask_cors import CORS
import sqlite3
import hashlib
import time
import random
import string
import os

app = Flask(__name__)
CORS(app)

DATABASE = 'liveo.db'

def init_db():
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    c.execute('''CREATE TABLE IF NOT EXISTS codes
                 (code TEXT PRIMARY KEY,
                  m3u_url TEXT,
                  duration_days INTEGER,
                  created_at INTEGER,
                  expires_at INTEGER,
                  is_active INTEGER,
                  customer_name TEXT,
                  device_id TEXT,
                  parental_pin TEXT)''')
    
    try:
        c.execute('ALTER TABLE codes ADD COLUMN parental_pin TEXT')
    except sqlite3.OperationalError:
        pass
    
    c.execute('''CREATE TABLE IF NOT EXISTS admin
                 (username TEXT PRIMARY KEY,
                  password_hash TEXT)''')
    
    admin_exists = c.execute('SELECT * FROM admin WHERE username=?', ('admin',)).fetchone()
    if not admin_exists:
        password_hash = hashlib.sha256('admin123'.encode()).hexdigest()
        c.execute('INSERT INTO admin VALUES (?, ?)', ('admin', password_hash))
    
    conn.commit()
    conn.close()

def generate_code():
    return ''.join(random.choices(string.ascii_uppercase + string.digits, k=8))

@app.route('/')
def index():
    return jsonify({'app': 'Liveo Backend', 'status': 'running', 'version': '1.0'})

@app.route('/admin/login', methods=['POST'])
def admin_login():
    data = request.json
    username = data.get('username')
    password = data.get('password')
    
    password_hash = hashlib.sha256(password.encode()).hexdigest()
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    result = c.execute('SELECT * FROM admin WHERE username=? AND password_hash=?', 
                      (username, password_hash)).fetchone()
    conn.close()
    
    return jsonify({'success': result is not None})

@app.route('/admin/create_code', methods=['POST'])
def create_code():
    data = request.json
    code = data.get('code') or generate_code()
    m3u_url = data.get('m3u_url', '')
    duration_days = int(data.get('duration_days', 30))
    customer_name = data.get('customer_name', '')
    parental_pin = data.get('parental_pin', '')
    
    created_at = int(time.time())
    expires_at = created_at + (duration_days * 24 * 60 * 60)
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    try:
        c.execute('''INSERT INTO codes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)''',
                  (code, m3u_url, duration_days, created_at, expires_at, 1, customer_name, '', parental_pin))
        conn.commit()
        return jsonify({
            'success': True, 
            'code': code,
            'parental_pin': parental_pin if parental_pin else None
        })
    except sqlite3.IntegrityError:
        return jsonify({'success': False, 'message': 'الكود موجود مسبقاً'})
    finally:
        conn.close()

@app.route('/admin/codes', methods=['GET'])
def get_codes():
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    codes = c.execute('SELECT * FROM codes ORDER BY created_at DESC').fetchall()
    conn.close()
    
    result = []
    for code in codes:
        result.append({
            'code': code[0],
            'm3u_url': code[1],
            'duration_days': code[2],
            'created_at': code[3],
            'expires_at': code[4],
            'is_active': code[5],
            'customer_name': code[6],
            'device_id': code[7]
        })
    
    return jsonify(result)

@app.route('/activate', methods=['POST'])
def activate():
    data = request.json
    code = data.get('code', '').upper()
    device_id = data.get('device_id', '')
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    result = c.execute('SELECT * FROM codes WHERE code=?', (code,)).fetchone()
    
    if not result:
        conn.close()
        return jsonify({'success': False, 'message': 'كود غير صحيح'})
    
    current_time = int(time.time())
    
    if result[4] < current_time:
        conn.close()
        return jsonify({'success': False, 'message': 'انتهت صلاحية الكود'})
    
    if result[5] == 0:
        conn.close()
        return jsonify({'success': False, 'message': 'الكود معطل'})
    
    stored_device_id = result[7]
    if stored_device_id and stored_device_id != device_id:
        conn.close()
        return jsonify({'success': False, 'message': 'الكود مستخدم على جهاز آخر'})
    
    if not stored_device_id:
        c.execute('UPDATE codes SET device_id=? WHERE code=?', (device_id, code))
        conn.commit()
    
    conn.close()
    
    return jsonify({
        'success': True,
        'code': result[0],
        'm3u_url': result[1],
        'expires_at': result[4],
        'customer_name': result[6],
        'days_left': (result[4] - current_time) // (24 * 60 * 60),
        'parental_pin': result[8] if len(result) > 8 else None
    })

@app.route('/admin/delete_code/<code>', methods=['DELETE'])
def delete_code(code):
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    c.execute('DELETE FROM codes WHERE code=?', (code,))
    conn.commit()
    conn.close()
    return jsonify({'success': True})

@app.route('/admin/toggle_code/<code>', methods=['POST'])
def toggle_code(code):
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    current = c.execute('SELECT is_active FROM codes WHERE code=?', (code,)).fetchone()
    if current:
        new_status = 0 if current[0] == 1 else 1
        c.execute('UPDATE codes SET is_active=? WHERE code=?', (new_status, code))
        conn.commit()
    conn.close()
    return jsonify({'success': True})

@app.route('/admin/reset_code/<code>', methods=['POST'])
def reset_code(code):
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    c.execute('UPDATE codes SET device_id=NULL WHERE code=?', (code,))
    conn.commit()
    conn.close()
    return jsonify({'success': True, 'message': 'تم إعادة تعيين الكود بنجاح'})

@app.route('/verify_pin', methods=['POST'])
def verify_pin():
    data = request.json
    code = data.get('code', '').upper()
    pin = data.get('pin', '')
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    result = c.execute('SELECT parental_pin FROM codes WHERE code=?', (code,)).fetchone()
    conn.close()
    
    if not result:
        return jsonify({'success': False, 'message': 'كود غير موجود'})
    
    stored_pin = result[0]
    
    if not stored_pin or stored_pin == '':
        return jsonify({
            'success': True,
            'valid': False,
            'message': 'الحماية غير مفعلة لهذا الحساب'
        })
    
    is_valid = (stored_pin == pin)
    
    return jsonify({
        'success': True,
        'valid': is_valid,
        'message': 'صحيح' if is_valid else 'رقم سري خاطئ'
    })

# صفحة Admin Panel مضمّنة
@app.route('/admin')
@app.route('/admin/')
@app.route('/admin-panel')
@app.route('/admin-panel/')
def admin_panel():
    html = '''<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liveo Admin Panel</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        body { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; direction: rtl; }
        .login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .login-box { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); width: 400px; }
        .logo { text-align: center; margin-bottom: 30px; }
        .logo h1 { color: #667eea; font-size: 36px; margin-bottom: 5px; }
        .logo p { color: #888; }
        input[type="text"], input[type="password"], input[type="number"] { width: 100%; padding: 12px; margin: 10px 0; border: 2px solid #e0e0e0; border-radius: 8px; font-size: 14px; transition: all 0.3s; }
        input:focus { outline: none; border-color: #667eea; }
        button { width: 100%; padding: 12px; background: #667eea; color: white; border: none; border-radius: 8px; font-size: 16px; cursor: pointer; margin-top: 10px; transition: all 0.3s; }
        button:hover { background: #5568d3; transform: translateY(-2px); }
        .admin-panel { padding: 20px; max-width: 1400px; margin: 0 auto; }
        .header { background: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
        .header h1 { color: #333; }
        .logout-btn { width: auto; padding: 10px 20px; background: #f44336; }
        .logout-btn:hover { background: #d32f2f; }
        .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 20px; }
        .stat-card { background: white; padding: 20px; border-radius: 10px; text-align: center; }
        .stat-card h3 { color: #888; margin-bottom: 10px; }
        .stat-card p { font-size: 32px; color: #667eea; font-weight: bold; }
        .create-code { background: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
        .form-group input { width: 100%; padding: 10px; border: 2px solid #e0e0e0; border-radius: 5px; }
        .form-group small { color: #888; font-size: 12px; }
        .codes-list { background: white; padding: 20px; border-radius: 10px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; text-align: right; border-bottom: 1px solid #e0e0e0; }
        th { background: #f5f5f5; font-weight: 600; color: #333; }
        .status-active { color: #4caf50; font-weight: bold; }
        .status-inactive { color: #f44336; font-weight: bold; }
        .action-btn { padding: 5px 10px; margin: 0 2px; border-radius: 5px; font-size: 12px; cursor: pointer; }
        .btn-delete { background: #f44336; }
        .btn-toggle { background: #ff9800; }
        .btn-reset { background: #FFA500; }
        .success-msg { background: #4caf50; color: white; padding: 10px; border-radius: 5px; margin-bottom: 15px; display: none; }
        #adminPanel { display: none; }
    </style>
</head>
<body>
    <div class="login-container" id="loginPage">
        <div class="login-box">
            <div class="logo"><h1>Liveo</h1><p>Admin Panel</p></div>
            <input type="text" id="username" placeholder="اسم المستخدم" value="admin">
            <input type="password" id="password" placeholder="كلمة المرور" value="admin123">
            <button onclick="login()">دخول</button>
        </div>
    </div>

    <div class="admin-panel" id="adminPanel">
        <div class="header">
            <h1>🎛️ Liveo Admin</h1>
            <button class="logout-btn" onclick="logout()">تسجيل خروج</button>
        </div>
        <div class="stats">
            <div class="stat-card"><h3>إجمالي الأكواد</h3><p id="totalCodes">0</p></div>
            <div class="stat-card"><h3>أكواد نشطة</h3><p id="activeCodes">0</p></div>
            <div class="stat-card"><h3>أكواد منتهية</h3><p id="expiredCodes">0</p></div>
        </div>
        <div class="create-code">
            <h2>➕ إنشاء كود جديد</h2>
            <div id="successMsg" class="success-msg"></div>
            <div class="form-group"><label>كود التفعيل (اختياري)</label><input type="text" id="newCode" placeholder="ABC12345"></div>
            <div class="form-group"><label>رابط M3U</label><input type="text" id="m3uUrl" placeholder="https://example.com/playlist.m3u" required></div>
            <div class="form-group"><label>المدة (أيام)</label><input type="number" id="duration" value="30" required></div>
            <div class="form-group"><label>اسم العميل</label><input type="text" id="customerName" placeholder="محمد"></div>
            <div class="form-group"><label>🔒 رقم سري (اختياري)</label><input type="text" id="parentalPin" placeholder="1234" maxlength="6"><small>للمحتوى البالغ</small></div>
            <button onclick="createCode()">إنشاء كود</button>
        </div>
        <div class="codes-list">
            <h2>📋 قائمة الأكواد</h2>
            <table><thead><tr><th>الكود</th><th>العميل</th><th>المدة</th><th>الإنشاء</th><th>الانتهاء</th><th>الحالة</th><th>الإجراءات</th></tr></thead><tbody id="codesTable"></tbody></table>
        </div>
    </div>

    <script>
        const API_URL = window.location.origin;
        function login() {
            fetch(`${API_URL}/admin/login`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username: document.getElementById('username').value, password: document.getElementById('password').value})
            }).then(r => r.json()).then(d => {
                if (d.success) {
                    document.getElementById('loginPage').style.display = 'none';
                    document.getElementById('adminPanel').style.display = 'block';
                    loadCodes();
                } else alert('خطأ في البيانات');
            });
        }
        function logout() {
            document.getElementById('loginPage').style.display = 'flex';
            document.getElementById('adminPanel').style.display = 'none';
        }
        function createCode() {
            const data = {
                code: document.getElementById('newCode').value,
                m3u_url: document.getElementById('m3uUrl').value,
                duration_days: document.getElementById('duration').value,
                customer_name: document.getElementById('customerName').value,
                parental_pin: document.getElementById('parentalPin').value || null
            };
            if (!data.m3u_url) return alert('أدخل رابط M3U');
            fetch(`${API_URL}/admin/create_code`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(data)
            }).then(r => r.json()).then(d => {
                if (d.success) {
                    let msg = `✅ الكود: ${d.code}`;
                    if (d.parental_pin) msg += `\\n🔒 الرقم: ${d.parental_pin}`;
                    alert(msg);
                    loadCodes();
                    document.getElementById('newCode').value = '';
                    document.getElementById('m3uUrl').value = '';
                    document.getElementById('customerName').value = '';
                    document.getElementById('parentalPin').value = '';
                }
            });
        }
        function loadCodes() {
            fetch(`${API_URL}/admin/codes`).then(r => r.json()).then(data => {
                const table = document.getElementById('codesTable');
                table.innerHTML = '';
                let total = 0, active = 0, expired = 0;
                data.forEach(c => {
                    total++;
                    const now = Date.now();
                    const isExpired = c.expires_at * 1000 < now;
                    if (c.is_active && !isExpired) active++;
                    if (isExpired) expired++;
                    const row = table.insertRow();
                    row.innerHTML = `<td>${c.code}</td><td>${c.customer_name || '-'}</td><td>${c.duration_days} يوم</td><td>${new Date(c.created_at * 1000).toLocaleDateString('ar')}</td><td>${new Date(c.expires_at * 1000).toLocaleDateString('ar')}</td><td class="${c.is_active ? 'status-active' : 'status-inactive'}">${c.is_active ? 'نشط' : 'معطل'}</td><td><button class="action-btn btn-reset" onclick="resetCode('${c.code}')">إعادة</button><button class="action-btn btn-toggle" onclick="toggleCode('${c.code}')">${c.is_active ? 'تعطيل' : 'تفعيل'}</button><button class="action-btn btn-delete" onclick="deleteCode('${c.code}')">حذف</button></td>`;
                });
                document.getElementById('totalCodes').textContent = total;
                document.getElementById('activeCodes').textContent = active;
                document.getElementById('expiredCodes').textContent = expired;
            });
        }
        function deleteCode(code) { if (confirm(`حذف ${code}؟`)) fetch(`${API_URL}/admin/delete_code/${code}`, {method: 'DELETE'}).then(() => loadCodes()); }
        function resetCode(code) { if (confirm(`إعادة تعيين ${code}؟`)) fetch(`${API_URL}/admin/reset_code/${code}`, {method: 'POST'}).then(() => loadCodes()); }
        function toggleCode(code) { fetch(`${API_URL}/admin/toggle_code/${code}`, {method: 'POST'}).then(() => loadCodes()); }
    </script>
</body>
</html>'''
    return html

if __name__ == '__main__':
    init_db()
    port = int(os.environ.get('PORT', 10000))
    app.run(debug=False, host='0.0.0.0', port=port)
