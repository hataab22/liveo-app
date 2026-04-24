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
                  device_id TEXT)''')
    
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
def home():
    return jsonify({
        'status': 'running',
        'app': 'Liveo Backend',
        'version': '1.0'
    })

@app.route('/admin/login', methods=['POST'])
def admin_login():
    data = request.json
    username = data.get('username')
    password = data.get('password')
    
    password_hash = hashlib.sha256(password.encode()).hexdigest()
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    admin = c.execute('SELECT * FROM admin WHERE username=? AND password_hash=?', 
                      (username, password_hash)).fetchone()
    conn.close()
    
    if admin:
        return jsonify({'success': True, 'message': 'تم تسجيل الدخول'})
    return jsonify({'success': False, 'message': 'خطأ في البيانات'})

@app.route('/admin/create_code', methods=['POST'])
def create_code():
    data = request.json
    code = data.get('code') or generate_code()
    m3u_url = data.get('m3u_url', '')
    duration_days = int(data.get('duration_days', 30))
    customer_name = data.get('customer_name', '')
    
    created_at = int(time.time())
    expires_at = created_at + (duration_days * 24 * 60 * 60)
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    try:
        c.execute('''INSERT INTO codes VALUES (?, ?, ?, ?, ?, ?, ?, ?)''',
                  (code, m3u_url, duration_days, created_at, expires_at, 1, customer_name, ''))
        conn.commit()
        return jsonify({'success': True, 'code': code})
    except sqlite3.IntegrityError:
        return jsonify({'success': False, 'message': 'الكود موجود بالفعل'})
    finally:
        conn.close()

@app.route('/admin/codes', methods=['GET'])
def get_all_codes():
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    codes = c.execute('SELECT * FROM codes ORDER BY created_at DESC').fetchall()
    conn.close()
    
    result = []
    current_time = int(time.time())
    
    for code in codes:
        result.append({
            'code': code[0],
            'm3u_url': code[1],
            'duration_days': code[2],
            'created_at': code[3],
            'expires_at': code[4],
            'is_active': code[5],
            'customer_name': code[6],
            'device_id': code[7],
            'days_left': max(0, (code[4] - current_time) // (24 * 60 * 60)),
            'is_expired': code[4] < current_time
        })
    
    return jsonify(result)

@app.route('/activate', methods=['POST'])
def activate_code():
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
        return jsonify({'success': False, 'message': 'الكود منتهي الصلاحية'})
    
    if result[5] == 0:
        conn.close()
        return jsonify({'success': False, 'message': 'الكود معطل'})
    
    if result[7] == '':
        c.execute('UPDATE codes SET device_id=? WHERE code=?', (device_id, code))
        conn.commit()
    elif result[7] != device_id:
        conn.close()
        return jsonify({'success': False, 'message': 'الكود مستخدم على جهاز آخر'})
    
    conn.close()
    
    return jsonify({
        'success': True,
        'code': result[0],
        'm3u_url': result[1],
        'expires_at': result[4],
        'customer_name': result[6],
        'days_left': (result[4] - current_time) // (24 * 60 * 60)
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

if __name__ == '__main__':
    init_db()
    port = int(os.environ.get('PORT', 10000))
    app.run(debug=False, host='0.0.0.0', port=port)
